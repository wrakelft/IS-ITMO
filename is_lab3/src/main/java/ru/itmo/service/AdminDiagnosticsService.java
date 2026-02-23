package ru.itmo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.itmo.util.HibernateUtil;

@ApplicationScoped
public class AdminDiagnosticsService {

    @Inject
    HibernateUtil hibernateUtil;

    public long dbHold(long ms) {
        long safe = Math.max(0, Math.min(ms, 10_000));
        try (var session = hibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            session.createNativeQuery("select 1").getSingleResult();
            Thread.sleep(safe);
            tx.commit();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        return safe;
    }
}