package se.plilja.loadingcache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Aspect
@Component
class LoadingCacheableAspect {
    private final LoadingCacheLoader loadingCacheLoader;
    private final Map<String, Boolean> handledCaches = new ConcurrentHashMap<>();

    LoadingCacheableAspect(LoadingCacheLoader loadingCacheLoader) {
        this.loadingCacheLoader = loadingCacheLoader;
    }

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object loadedCached(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Cacheable loadingCacheableAnnotation = methodSignature.getMethod().getAnnotation(Cacheable.class);
        String[] names = loadingCacheableAnnotation.cacheNames().length > 0 ? loadingCacheableAnnotation.cacheNames() : loadingCacheableAnnotation.value();
        for (String cacheName : names) {
            handledCaches.computeIfAbsent(cacheName, (name) -> {
                loadingCacheLoader.addLoader(method, key -> {
                    return ReflectionMethodInvoker.invoke(key.getTarget(), key.getMethod(), key.getArgs());
                });
                return true;
            });
        }
        return joinPoint.proceed();
    }

}
