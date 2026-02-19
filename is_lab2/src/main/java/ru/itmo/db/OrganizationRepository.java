package ru.itmo.db;

import ru.itmo.dto.OrganizationRequestDTO;
import ru.itmo.mapper.OrganizationMapper;
import ru.itmo.model.Address;
import ru.itmo.model.Coordinates;
import ru.itmo.model.Organization;
import ru.itmo.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class OrganizationRepository {

    @Inject
    private HibernateUtil hibernateUtil;
    @Inject
    private OrganizationMapper organizationMapper;
    @Inject
    private CoordinatesRepository coordinatesRepository;
    @Inject
    private AddressRepository addressRepository;

    public Organization createFromDtoInSession(OrganizationRequestDTO dto, Session session) {
        Organization org = organizationMapper.toNewForCreate(dto);
        Coordinates coords;
        if(dto.getCoordinatesId() != null) {
            coords = session.get(Coordinates.class, dto.getCoordinatesId());
            if (coords == null) throw new IllegalArgumentException("Координаты не найдены");
        } else {
            coords = coordinatesRepository.createFromDto(dto.getCoordinates(), session);
        }
        org.setCoordinates(coords);
        if (org.getCreationDate() == null) {
            org.setCreationDate(LocalDateTime.now());
        }

        Address official;
        if (dto.getOfficialAddressId() != null) {
            official = session.get(Address.class, dto.getOfficialAddressId());
            if (official == null) throw new IllegalArgumentException("Официальный адрес не найден");
        } else {
            official = addressRepository.createFromDtoInSession(dto.getOfficialAddress(), session);
        }
        org.setOfficialAddress(official);

        Address postal = null;
        if (dto.getPostalAddressId() != null && dto.getPostalAddress() != null) {
            throw new IllegalArgumentException("Укажите адреса");
        }
        if (dto.getPostalAddressId() != null) {
            postal = session.get(Address.class, dto.getPostalAddressId());
            if (postal == null) throw new IllegalArgumentException("Почтовый адрес не найден");
        }
        else if (dto.getPostalAddress() != null) {
            postal = addressRepository.createFromDtoInSession(dto.getPostalAddress(), session);
        }
        org.setPostalAddress(postal);
        return org;
    }

    public Organization updateFromDtoInSession(Long id, OrganizationRequestDTO dto, Session session) {
        Organization existing = session.get(Organization.class, id);
        if (existing == null) throw new IllegalArgumentException("Организация не найдена");
        organizationMapper.applyBasicToExisting(dto, existing);
        if (dto.getCoordinatesId() != null || dto.getCoordinates() != null) {
            Coordinates coords;
            if (dto.getCoordinatesId() != null) {
                coords = session.get(Coordinates.class, dto.getCoordinatesId());
                if (coords == null) throw new IllegalArgumentException("Координаты не найдены");
            } else {
                coords = coordinatesRepository.createFromDto(dto.getCoordinates(), session);
            }
            existing.setCoordinates(coords);
        }
        if (dto.getOfficialAddressId() != null || dto.getOfficialAddress() != null) {
            Address official;
            if (dto.getOfficialAddressId() != null) {
                official = session.get(Address.class, dto.getOfficialAddressId());
                if (official == null) throw new IllegalArgumentException("Официальный адрес не найден");
            } else {
                official = addressRepository.createFromDtoInSession(dto.getOfficialAddress(), session);
            }
            existing.setOfficialAddress(official);
        }
        if (dto.isPostalAddressProvided() || dto.isPostalAddressIdProvided()) {
            Address postal = null;
            if (dto.getPostalAddressId() != null) {
                postal = session.get(Address.class, dto.getPostalAddressId());
                if (postal == null) throw new IllegalArgumentException("Почтовый адрес не найден");
            } else if (dto.getPostalAddress() != null) {
                postal = addressRepository.createFromDtoInSession(dto.getPostalAddress(), session);
            } else {
                postal = null;
            }
            existing.setPostalAddress(postal);
        }
        return existing;
    }

    public Organization findById(Long id) {
        Objects.requireNonNull(id, "id не может быть 0");
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                select distinct o
                from Organization o
                left join fetch o.coordinates
                left join fetch o.officialAddress
                left join fetch o.postalAddress
                where o.id = :id
            """, Organization.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }


    public List<Organization> findAll() {
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                select distinct o
                from Organization o
                left join fetch o.coordinates
                left join fetch o.officialAddress
                left join fetch o.postalAddress
            """, Organization.class).getResultList();
        }
    }

    public void delete(Long id) {
        Objects.requireNonNull(id, "id 0");
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Organization organization = session.find(Organization.class, id);
                if (organization != null) session.remove(organization);
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }


    public List<Organization> findByCoordinatesId(Long coordinatesId) {
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Organization o WHERE o.coordinates.id = :coordinatesId",
                    Organization.class)
                    .setParameter("coordinatesId", coordinatesId)
                    .getResultList();
        }
    }


    public List<Organization> findByOfficialAddressId(Long addressId) {
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM Organization o WHERE o.officialAddress.id = :addressId",
                    Organization.class)
                    .setParameter("addressId", addressId)
                    .getResultList();
        }
    }


    public Double getAverageRating() {
        try(Session session = hibernateUtil.getSessionFactory().openSession()) {
            Double avg = session.createQuery("select avg(o.rating) from Organization o", Double.class).uniqueResult();
            return avg != null ? avg : 0.0;
        }
    }


    public Organization getWithMinRating() {
        try(Session session = hibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
                select distinct o
                from Organization o
                left join fetch o.coordinates
                left join fetch o.officialAddress
                left join fetch o.postalAddress
                order by o.rating asc
            """, Organization.class)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }


    public List<Organization> getWithRatingGreaterThan(int rating) {
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("""
               select distinct o
               from Organization o
               left join fetch o.coordinates
               left join fetch o.officialAddress
               left join fetch o.postalAddress
               where o.rating > :r
            """, Organization.class)
                    .setParameter("r", rating)
                    .getResultList();
        }
    }

    public int fireAllEmployees(long orgId) {
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
               int updated = session.createQuery("update Organization o set o.employeesCount = 0 where o.id = :id")
                        .setParameter("id", orgId)
                        .executeUpdate();
                tx.commit();
                return updated;
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }


    public int hireEmployee(long orgId) {
        try (Session session = hibernateUtil.getSessionFactory().openSession()){
            Transaction tx = session.beginTransaction();
            try {
            int updated = session.createQuery("update Organization o set o.employeesCount = o.employeesCount + 1 where o.id = :id")
                    .setParameter("id", orgId)
                    .executeUpdate();
            tx.commit();
            return updated;
            } catch (RuntimeException e) {
                if (tx != null) tx.rollback();
                throw e;
            }
        }
    }

    private String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    public void ensureUniqueAddressAndCoords(Session session, String street, Float x, Long y, Long excludeId) {
        String st = norm(street);
        if (st.isBlank() || x == null || y == null) return;

        String hql = """
        select count(o.id)
        from Organization o
        join o.coordinates c
        join o.officialAddress a
        where lower(trim(a.street)) = :st
          and c.x = :x
          and c.y = :y
    """;

        if (excludeId != null) {
            hql += " and o.id <> :id";
        }

        var q = session.createQuery(hql, Long.class)
                .setParameter("st", st)
                .setParameter("x", x)
                .setParameter("y", y);

        if (excludeId != null) q.setParameter("id", excludeId);

        Long cnt = q.uniqueResult();
        if (cnt != null && cnt > 0) {
            throw new IllegalArgumentException(
                    "Пара (officialAddress.street, coordinates.x, coordinates.y) должна быть уникальна"
            );
        }
    }

    public void ensureUniqueName(Session session, String name, Long excludeId) {
        String n = norm(name);
        if (n.isBlank()) return;

        String hql = """
        select count(o.id)
        from Organization o
        where lower(trim(o.name)) = :n
    """;

        if (excludeId != null) {
            hql += " and o.id <> :id";
        }

        var q = session.createQuery(hql, Long.class)
                .setParameter("n", n);

        if (excludeId != null) q.setParameter("id", excludeId);

        Long cnt = q.uniqueResult();
        if (cnt != null && cnt > 0) {
            throw new IllegalArgumentException(
                    "Название организации должно быть уникальным (без учета регистра и пробелов)"
            );
        }
    }
}
