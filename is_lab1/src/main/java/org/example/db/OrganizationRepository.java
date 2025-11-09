package org.example.db;

import org.example.model.Address;
import org.example.model.Coordinates;
import org.example.model.Organization;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class OrganizationRepository {

    @Inject
    private HibernateUtil hibernateUtil;

    public Long save(Organization organization) {
        Session session = hibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.persist(organization);
            tx.commit();
            return organization.getId();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error saving organization", e);
        } finally {
            session.close();
        }
    }

    public Organization findById(Long id) {
        Session session = hibernateUtil.getSessionFactory().openSession();
        try {
            return session.find(Organization.class, id);
        } finally {
            session.close();
        }
    }

    public List<Organization> findAll() {
        Session session = hibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Organization", Organization.class).getResultList();
        } finally {
            session.close();
        }
    }

    public void update(Organization organization) {
        Session session = hibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.merge(organization);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error updating organization", e);
        } finally {
            session.close();
        }
    }

    public void delete(Long id, boolean cascadeRelated) {
        Objects.requireNonNull(id, "id is null");
        Session session = hibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Organization organization = session.find(Organization.class, id);
            if (organization == null) {
                tx.commit();
                return;
            }
            Address addr = organization.getOfficialAddress();
            Coordinates coords = organization.getCoordinates();

            session.remove(organization);
            session.flush();

            if (cascadeRelated) {
                if (addr != null && addr.getId() != null) {
                    Long addrId = addr.getId();
                    Long cnt = session.createQuery(
                                    "select count(o.id) from Organization o where o.officialAddress.id = :aId", Long.class)
                            .setParameter("aId", addrId)
                            .uniqueResult();
                    long usage = cnt == null ? 0L : cnt;
                    if (usage > 0L) {
                        Address managedAddr = session.find(Address.class, addrId);
                        if (managedAddr != null) session.remove(managedAddr);
                    }
                }
                if (coords != null && coords.getId() != null) {
                    Long coordsId = coords.getId();
                    Long cnt = session.createQuery(
                                    "select count(o.id) from Organization o where o.coordinates.id = :cId", Long.class)
                            .setParameter("cId", coordsId)
                            .uniqueResult();
                    long usage = cnt == null ? 0L : cnt;
                    if (usage > 0L) {
                        Coordinates managedCoords = session.find(Coordinates.class, coordsId);
                        if (managedCoords != null) session.remove(managedCoords);
                    }
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error deleting organization", e);
        } finally {
            session.close();
        }
    }

    public List<Organization> findByCoordinatesId(Long coordinatesId) {
        Session session = hibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                    "FROM Organization o WHERE o.coordinates.id = :coordinatesId",
                    Organization.class)
                    .setParameter("coordinatesId", coordinatesId)
                    .getResultList();
        } finally {
            session.close();
        }
    }

    public List<Organization> findByOfficialAddressId(Long addressId) {
        Session session = hibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                    "FROM Organization o WHERE o.officialAddress.id = :addressId",
                    Organization.class)
                    .setParameter("addressId", addressId)
                    .getResultList();
        } finally {
            session.close();
        }
    }
}
