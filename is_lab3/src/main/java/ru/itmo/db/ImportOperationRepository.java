package ru.itmo.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.itmo.model.ImportOperation;
import ru.itmo.util.HibernateUtil;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class ImportOperationRepository {
    @Inject HibernateUtil hibernateUtil;

    public long create(String username, String role, String entityType) {
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                ImportOperation op = new ImportOperation();
                op.setUsername(username);
                op.setRole(role);
                op.setEntityType(entityType);
                op.setStatus("RUNNING");
                op.setStartedAt(LocalDateTime.now());
                op.setFinishedAt(null);
                op.setAddedCount(null);
                op.setErrorMessage(null);

                session.save(op);
                tx.commit();
                return op.getId();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public void markAsSuccess(long opId, int addedCount) {
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                ImportOperation op = session.get(ImportOperation.class, opId);
                if (op == null) { tx.commit(); return; }

                op.setStatus("SUCCESS");
                op.setAddedCount(addedCount);
                op.setFinishedAt(LocalDateTime.now());
                op.setErrorMessage(null);

                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public void markAsFailed(long opId, String errorMessage) {
        try (Session session = hibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                ImportOperation op = session.get(ImportOperation.class, opId);
                if (op == null) { tx.commit(); return; }
                op.setStatus("FAILED");
                op.setAddedCount(null);
                op.setFinishedAt(LocalDateTime.now());
                op.setErrorMessage(shortMsg(errorMessage));
                tx.commit();
            } catch (RuntimeException e) {
                tx.rollback();
                throw e;
            }
        }
    }

    public List<ImportOperation> findAll(int limit) {
        int lim = (limit <= 0 || limit > 500) ? 100 : limit;
        try (Session s = hibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("""
                from ImportOperation op
                order by op.id desc
            """, ImportOperation.class)
                    .setMaxResults(lim)
                    .getResultList();
        }
    }

    public List<ImportOperation> findByUser(String username, int limit) {
        int lim = (limit <= 0 || limit > 500) ? 100 : limit;
        try (Session s = hibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("""
                from ImportOperation op
                where op.username = :u
                order by op.id desc
            """, ImportOperation.class)
                    .setParameter("u", username)
                    .setMaxResults(lim)
                    .getResultList();
        }
    }

    private String shortMsg(String m) {
        if (m == null) return null;
        m = m.replace("\r", " ").replace("\n", " ").trim();
        if (m.isBlank()) return null;
        return m.length() > 500 ? m.substring(0, 500) : m;
    }
}
