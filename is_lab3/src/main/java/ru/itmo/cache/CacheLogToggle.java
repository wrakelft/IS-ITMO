package ru.itmo.cache;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.itmo.util.HibernateStatsLogSwitch;

@ApplicationScoped
public class CacheLogToggle {
    @Inject
    HibernateStatsLogSwitch statsLogSwitch;

    private volatile boolean enabled = false;
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        statsLogSwitch.setEnabled(enabled);
    }
}