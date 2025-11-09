package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HibernateUtil {

    private SessionFactory sessionFactory;

    public HibernateUtil() {
        try {
            StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml")
                    .build();

            Metadata metadata = new MetadataSources(standardServiceRegistry)
                    .addResource("Organization.hbm.xml")
                    .addResource("Coordinates.hbm.xml")
                    .addResource("Address.hbm.xml")
                    .getMetadataBuilder()
                    .build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Hibernate SF", e);
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @PreDestroy
    public void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
