package ru.itmo.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import ru.itmo.cache.CacheLogToggle;
import ru.itmo.service.AdminDiagnosticsService;
import ru.itmo.service.CacheStatsService;
import ru.itmo.util.HibernateUtil;

import java.util.Map;


@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminController {

    @Inject
    private HibernateUtil hibernateUtil;
    @Inject
    private CacheLogToggle cacheLogToggle;
    @Inject
    AdminDiagnosticsService diagnosticsService;
    @Inject
    CacheStatsService cacheStatsService;

    @GET
    @Path("/pool")
    public Map<String, Object> stats() {
        var ds = hibernateUtil.getDataSource();
        var pool = ds.getConnectionPool();

        return Map.of(
                "active", ds.getNumActive(),
                "idle", ds.getNumIdle(),
                "waiters", pool != null ? pool.getNumWaiters() : 0,
                "maxTotal", ds.getMaxTotal()
        );
    }

    @GET
    @Path("/db-hold")
    public Map<String, Object> hold(@QueryParam("ms") @DefaultValue("2000") long ms) {
        long held = diagnosticsService.dbHold(ms);
        return Map.of("heldMs", held);
    }

    @GET
    @Path("/cache-logging")
    public Map<String, Object> cacheLoggingStatus() {
        return Map.of("enabled", cacheLogToggle.isEnabled());
    }

    @POST
    @Path("/cache-logging")
    public Map<String, Object> setCacheLogging(@QueryParam("enabled") @DefaultValue("false") boolean enabled) {
        cacheLogToggle.setEnabled(enabled);
        return Map.of("enabled", enabled);
    }

    @GET
    @Path("/cache-stats")
    public Map<String, Object> cacheStats() {
        return cacheStatsService.getStats();
    }

    @POST
    @Path("/cache-stats/reset")
    public Map<String, Object> resetCacheStats() {
        cacheStatsService.reset();
        return Map.of("reset", true);
    }
}