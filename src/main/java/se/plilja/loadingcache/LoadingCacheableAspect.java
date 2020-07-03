package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
class LoadingCacheableAspect {
    private final Map<String, LoadingCache<CacheKey, Object>> caches = new ConcurrentHashMap<>();
    private final CaffeineCaches caffeineCaches;

    LoadingCacheableAspect(CaffeineCaches caffeineCaches) {
        this.caffeineCaches = caffeineCaches;
    }

    @Around("@annotation(se.plilja.loadingcache.LoadingCacheable)")
    public Object loadedCached(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        LoadingCacheable loadingCacheableAnnotation = methodSignature.getMethod().getAnnotation(LoadingCacheable.class);
        CaffeineCache cache = caffeineCaches.getOrCreateLoadingCache(
                loadingCacheableAnnotation.value(),
                key -> {
                    if (key instanceof CacheKey) {
                        CacheKey cacheKey = (CacheKey) key;
                        return ReflectionMethodInvoker.invoke(cacheKey.getTarget(), cacheKey.getMethod(), cacheKey.getArgs());
                    } else {
                        return null;
                    }
                });
        CacheKey cacheKey = new CacheKey(joinPoint.getArgs(), joinPoint.getTarget(), method);
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
        if (valueWrapper != null) {
            return valueWrapper.get();
        } else {
            return null;
        }
    }

}
