package ru.itmo.util;

import org.apache.commons.dbcp2.BasicDataSource;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Properties;

@ApplicationScoped
public class HibernateUtil {

    private final SessionFactory sessionFactory;
    private final BasicDataSource dataSource;

    public HibernateUtil() {
        try {
            Properties p = loadProps("db.properties");

            dataSource = new BasicDataSource();
            dataSource.setDriverClassName(p.getProperty("db.driver", "org.postgresql.Driver"));
            dataSource.setUrl(req(p, "db.url"));
            dataSource.setUsername(req(p, "db.user"));
            dataSource.setPassword(req(p, "db.pass"));

            dataSource.setInitialSize(pInt(p, "pool.initialSize", 5));
            dataSource.setMaxTotal(pInt(p, "pool.maxTotal", 20));
            dataSource.setMinIdle(pInt(p, "pool.minIdle", 5));
            dataSource.setMaxIdle(pInt(p, "pool.maxIdle", 10));
            dataSource.setMaxWait(Duration.ofMillis(pInt(p, "pool.maxWaitMillis", 10_000)));

            dataSource.setValidationQuery(p.getProperty("pool.validationQuery", "SELECT 1"));
            dataSource.setTestOnBorrow(pBool(p, "pool.testOnBorrow", true));
            dataSource.setTestWhileIdle(pBool(p, "pool.testWhileIdle", true));

            dataSource.setDurationBetweenEvictionRuns(
                    Duration.ofMillis(pInt(p, "pool.timeBetweenEvictionRunsMillis", 30_000))
            );
            dataSource.setMinEvictableIdleTimeMillis(pInt(p, "pool.minEvictableIdleTimeMillis", 300_000));



            StandardServiceRegistryBuilder standardServiceRegistry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml");
            standardServiceRegistry.applySetting("hibernate.connection.provider_class",
                    "org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl");
            standardServiceRegistry.applySetting("hibernate.connection.datasource", dataSource);

            var ehcacheUrl = HibernateUtil.class.getClassLoader().getResource("ehcache.xml");
            if (ehcacheUrl == null) throw new IllegalStateException("ehcache.xml not found");
            standardServiceRegistry.applySetting("hibernate.javax.cache.uri", ehcacheUrl.toURI().toString());

            StandardServiceRegistry registry = standardServiceRegistry.build();

            Metadata metadata = new MetadataSources(registry)
                    .addResource("Organization.hbm.xml")
                    .addResource("Coordinates.hbm.xml")
                    .addResource("Address.hbm.xml")
                    .addResource("ImportOperation.hbm.xml")
                    .getMetadataBuilder()
                    .build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
            sessionFactory.getStatistics().setStatisticsEnabled(true);
        } catch (Exception e) {
            e.printStackTrace(); // <-- временно для отладки
            throw new RuntimeException("Failed to create Hibernate SF with DBCP2", e);
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public BasicDataSource getDataSource() {
        return dataSource;
    }

    @PreDestroy
    public void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException ignored) {}
        }
    }

    private static Properties loadProps(String resourceName) {
        Properties p = new Properties();
        try (InputStream in = HibernateUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + resourceName);
            p.load(in);
            return p;
        } catch (Exception e) {
            throw new IllegalStateException("Can't load " + resourceName, e);
        }
    }

    private static String req(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) throw new IllegalStateException("Missing property: " + key);
        return v.trim();
    }

    private static int pInt(Properties p, String key, int def) {
        String v = p.getProperty(key);
        return (v == null || v.isBlank()) ? def : Integer.parseInt(v.trim());
    }

    private static boolean pBool(Properties p, String key, boolean def) {
        String v = p.getProperty(key);
        return (v == null || v.isBlank()) ? def : Boolean.parseBoolean(v.trim());
    }
}
