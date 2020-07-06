package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class ConfigurableCaffeineCacheManager implements CacheManager {
    private final Map<String, Caffeine<Object, Object>> customizedCacheNames = new ConcurrentHashMap<>();
    private final LoadingCacheLoader loadingCacheLoader = new LoadingCacheLoader();
    private final Caffeine<Object, Object> defaultCaffeine;
    private final Map<String, Cache> caches = new ConcurrentHashMap<>();
    private final Set<String> canLoad = ConcurrentHashMap.newKeySet();

    ConfigurableCaffeineCacheManager(CaffeineSpec defaultSpec) {
        if (defaultSpec == null) {
            this.defaultCaffeine = Caffeine.newBuilder();
        } else {
            this.defaultCaffeine = Caffeine.from(defaultSpec);
        }
    }

    public void setCacheConfiguration(String name, Caffeine<Object, Object> caffeine) {
        customizedCacheNames.put(name, caffeine);
    }

    public void setCacheConfiguration(String name, CaffeineSpec caffeineSpec) {
        setCacheConfiguration(name, Caffeine.from(caffeineSpec));
    }

    void setLoadingCacheLoader(List<String> caches, Method method, Function<CacheKey, Object> loader) {
        loadingCacheLoader.addLoader(method, loader);
        canLoad.addAll(caches);
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, (_name) -> {
            CaffeineCache createdCache;
            if (customizedCacheNames.containsKey(name)) {
                Caffeine<Object, Object> caffeine = customizedCacheNames.get(name);
                createdCache = createCache(name, caffeine);
            } else {
                createdCache = createCache(name, defaultCaffeine);
            }
            return createdCache;
        });
    }

    private CaffeineCache createCache(String name, Caffeine<Object, Object> fromCaffeine) {
        CaffeineCache caffeineCache;
        if (canLoad.contains(name)) {
            caffeineCache = new CaffeineCache(name, fromCaffeine.build(loadingCacheLoader));
        } else {
            caffeineCache = new CaffeineCache(name, fromCaffeine.build());
        }
        return caffeineCache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
}
