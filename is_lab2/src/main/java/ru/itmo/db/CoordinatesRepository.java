package ru.itmo.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.dto.CoordinatesDTO;
import ru.itmo.model.Coordinates;
import ru.itmo.util.HibernateUtil;

import java.util.List;

@ApplicationScoped
public class CoordinatesRepository {

    @Inject
    private HibernateUtil hibernateUtil;

    public List<Coordinates> findAll() {
        try (Session s = hibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from Coordinates c order by c.id desc", Coordinates.class)
                    .getResultList();
        }
    }

    public Coordinates createFromDto(CoordinatesDTO dto, Session s) {
        if (dto == null)
            throw new IllegalArgumentException("Требуются координаты");

        if (dto.getX() == null)
            throw new IllegalArgumentException("Требуется координата.x");

        if (dto.getY() == null)
            throw new IllegalArgumentException("Требуется координата.y");

        Coordinates existing = s.createQuery("""
            select c from Coordinates c
            where c.x = :x and c.y = :y
            order by c.id asc
        """, Coordinates.class)
                .setParameter("x", dto.getX())
                .setParameter("y", dto.getY())
                .setMaxResults(1)
                .uniqueResult();

        if (existing != null) {
            return existing;
        }

        Coordinates c = new Coordinates();
        c.setX(dto.getX());
        c.setY(dto.getY());
        s.save(c);
        return c;
    }

    public void deleteWithReplace(Long id, Long replaceWithId) {
        try (Session s = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = s.beginTransaction();

            Coordinates old = s.get(Coordinates.class, id);
            if (old == null) { tx.commit(); return; }

            Long used = s.createQuery(
                    "select count(o.id) from Organization o where o.coordinates.id = :id",
                    Long.class
            ).setParameter("id", id).uniqueResult();

            long usage = used == null ? 0L : used;

            if (usage > 0 && replaceWithId == null) {
                tx.rollback();
                throw new IllegalArgumentException("Coordinates is used. Provide replaceWith");
            }

            if (usage > 0) {
                Coordinates rep = s.get(Coordinates.class, replaceWithId);
                if (rep == null) { tx.rollback(); throw new IllegalArgumentException("Replacement not found"); }
                if (rep.getId().equals(id)) { tx.rollback(); throw new IllegalArgumentException("replaceWith must differ"); }

                Long conflict = s.createQuery("""
                    select count(o.id)
                    from Organization o
                    join o.officialAddress a
                    where o.coordinates.id = :newId
                      and lower(trim(a.street)) in (
                          select lower(trim(a1.street))
                          from Organization o1
                          join o1.officialAddress a1
                          where o1.coordinates.id = :oldId)
                """, Long.class)
                        .setParameter("oldId", id)
                        .setParameter("newId", replaceWithId)
                        .uniqueResult();

                if (conflict != null && conflict > 0) {
                    tx.rollback();
                    throw new IllegalArgumentException(
                            "Нельзя перепривязать: появится дубликат пары (officialAddress.street, coordinates.x, coordinates.y)"
                    );
                }



                s.createQuery("update Organization o set o.coordinates = :rep where o.coordinates.id = :oldId")
                        .setParameter("rep", rep)
                        .setParameter("oldId", id)
                        .executeUpdate();
            }

            s.remove(old);
            tx.commit();
        }
    }
}

