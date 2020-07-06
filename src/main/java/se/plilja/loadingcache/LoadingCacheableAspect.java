package se.plilja.loadingcache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
class LoadingCacheableAspect {
    private final ConfigurableCaffeineCacheManager configurableCaffeineCacheManager;
    private final Map<Method, Boolean> handledCaches = new ConcurrentHashMap<>();

    LoadingCacheableAspect(ConfigurableCaffeineCacheManager configurableCaffeineCacheManager) {
        this.configurableCaffeineCacheManager = configurableCaffeineCacheManager;
    }

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object loadedCached(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        handledCaches.computeIfAbsent(method, (_ignored) -> {
            configurableCaffeineCacheManager.setLoadingCacheLoader(method, key -> {
                try {
                    if (!method.canAccess(key.getTarget())) {
                        method.setAccessible(true);
                    }
                    return method.invoke(key.getTarget(), key.getArgs());
                } catch (Throwable throwable) {
                    if (throwable instanceof RuntimeException) {
                        throw (RuntimeException) throwable;
                    } else if (throwable instanceof Error) {
                        throw (Error) throwable;
                    } else {
                        throw new RuntimeException("Caught exception while calling method", throwable);
                    }
                }
            });
            return true;
        });
        return joinPoint.proceed();
    }

}
