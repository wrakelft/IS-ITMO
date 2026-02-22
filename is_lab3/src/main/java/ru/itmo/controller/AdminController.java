package ru.itmo.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import ru.itmo.util.HibernateUtil;

import java.util.Map;

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
public class AdminController {

    @Inject
    private HibernateUtil hibernateUtil;

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
        try (var session = hibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();

            session.createNativeQuery("select 1").getSingleResult();

            Thread.sleep(ms);

            tx.commit();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        return Map.of("heldMs", ms);
    }
}