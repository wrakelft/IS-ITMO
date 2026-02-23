package ru.itmo.cache;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import ru.itmo.util.HibernateUtil;

import java.util.logging.Logger;

@LogL2Stats
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class L2StatsInterceptor {

    private static final Logger log = Logger.getLogger(L2StatsInterceptor.class.getName());

    @Inject private CacheLogToggle toggle;
    @Inject private HibernateUtil hibernateUtil;

    @AroundInvoke
    public Object around(InvocationContext ctx) throws Exception {
        if (!toggle.isEnabled()) return ctx.proceed();

        var st = hibernateUtil.getSessionFactory().getStatistics();
        long hit0 = st.getSecondLevelCacheHitCount();
        long miss0 = st.getSecondLevelCacheMissCount();
        long put0 = st.getSecondLevelCachePutCount();

        Object res = ctx.proceed();

        long hit1 = st.getSecondLevelCacheHitCount();
        long miss1 = st.getSecondLevelCacheMissCount();
        long put1 = st.getSecondLevelCachePutCount();

        log.info(() -> "[L2]" + ctx.getMethod().getName()
                + " | hit +" + (hit1 - hit0)
                + " miss +" + (miss1 - miss0)
                + " put +" + (put1 - put0));
        return res;
    }
}
