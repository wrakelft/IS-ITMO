package ru.itmo.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.itmo.util.HibernateUtil;

import java.util.Map;

@ApplicationScoped
public class CacheStatsService {

    @Inject
    HibernateUtil hibernateUtil;

    public Map<String, Object> getStats() {
        var st = hibernateUtil.getSessionFactory().getStatistics();
        return Map.of(
                "l2Hit", st.getSecondLevelCacheHitCount(),
                "l2Miss", st.getSecondLevelCacheMissCount(),
                "l2Put", st.getSecondLevelCachePutCount()
        );
    }

    public void reset() {
        hibernateUtil.getSessionFactory().getStatistics().clear();
    }
}