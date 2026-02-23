package ru.itmo.util;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class HibernateStatsLogSwitch {

    private static final String CAT =
            "org.hibernate.engine.internal.StatisticalLoggingSessionEventListener";

    public void setEnabled(boolean enabled) {
        Logger.getLogger(CAT).setLevel(enabled ? Level.INFO : Level.WARNING);
    }
}