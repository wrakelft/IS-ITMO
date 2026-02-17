package ru.itmo.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.dto.AddressDTO;
import ru.itmo.model.Address;
import ru.itmo.util.HibernateUtil;

import java.util.List;

@ApplicationScoped
public class AddressRepository {

    @Inject
    private HibernateUtil hibernateUtil;

    public List<Address> findAll() {
        try (Session s = hibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Address a order by a.id desc", Address.class).getResultList();
        }
    }

    public Address createFromDtoInSession(AddressDTO dto, Session s) {
        if (dto == null || dto.getStreet() == null || dto.getStreet().trim().isEmpty())
            throw new WebApplicationException("Требуется улица", 400);

        String street = dto.getStreet().trim();
        String streetNorm = street.toLowerCase();

        Address existing = s.createQuery("""
            select a from Address a
            where lower(trim(a.street)) = :st
            order by a.id asc
        """, Address.class)
                .setParameter("st", streetNorm)
                .setMaxResults(1)
                .uniqueResult();

        if (existing != null) {
            return existing;
        }
        Address a = new Address();
        a.setStreet(dto.getStreet().trim());
        s.save(a);
        return a;
    }

    public void deleteWithReplace(Long id, Long replaceWithId) {
        try (Session s = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();

            Address old = s.get(Address.class, id);
            if (old == null) { tx.commit(); return; }

            Long usedOfficial = s.createQuery(
                    "select count(o.id) from Organization o where o.officialAddress.id = :id",
                    Long.class).setParameter("id", id).uniqueResult();
            Long usedPostal = s.createQuery(
                    "select count(o.id) from Organization o where o.postalAddress.id = :id",
                    Long.class).setParameter("id", id).uniqueResult();

            long used = (usedOfficial == null ? 0 : usedOfficial) + (usedPostal == null ? 0 : usedPostal);

            if (used > 0 && replaceWithId == null) {
                tx.rollback();
                throw new WebApplicationException("Address is used. Provide replaceWith", 409);
            }

            if (used > 0) {
                Address rep = s.get(Address.class, replaceWithId);
                if (rep == null) { tx.rollback(); throw new WebApplicationException("Replacement not found", 404); }
                if (rep.getId().equals(id)) { tx.rollback(); throw new WebApplicationException("replaceWith must differ", 400); }

                s.createQuery("update Organization o set o.officialAddress = :rep where o.officialAddress.id = :oldId")
                        .setParameter("rep", rep).setParameter("oldId", id).executeUpdate();
                s.createQuery("update Organization o set o.postalAddress = :rep where o.postalAddress.id = :oldId")
                        .setParameter("rep", rep).setParameter("oldId", id).executeUpdate();
            }

            s.remove(old);
            tx.commit();
        }
    }
}

