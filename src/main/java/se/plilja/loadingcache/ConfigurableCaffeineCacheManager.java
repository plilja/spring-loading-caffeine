package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class ConfigurableCaffeineCacheManager implements CacheManager {
    private final CaffeineCacheManager caffeineCacheManager; // TODO consider removing
    private final Map<String, Caffeine<Object, Object>> customizedCacheNames = new ConcurrentHashMap<>();
    private final LoadingCacheLoader loadingCacheLoader = new LoadingCacheLoader();
    private final Caffeine<Object, Object> defaultCaffeine;
    private final Set<String> cacheNames = new HashSet<>();
    private final Set<String> canLoad = ConcurrentHashMap.newKeySet();

    ConfigurableCaffeineCacheManager(CaffeineSpec defaultSpec) {
        if (defaultSpec == null) {
            this.defaultCaffeine = Caffeine.newBuilder();
        } else {
            this.defaultCaffeine = Caffeine.from(defaultSpec);
        }
        caffeineCacheManager = new CaffeineCacheManager();
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
        if (!cacheNames.contains(name)) {
            synchronized (this) {
                // double checked locking
                if (!cacheNames.contains(name)) {
                    cacheNames.add(name);
                    if (customizedCacheNames.containsKey(name)) {
                        Caffeine<Object, Object> caffeine = customizedCacheNames.get(name);
                        if (canLoad.contains(name)) {
                            caffeineCacheManager.registerCustomCache(name, caffeine.build(loadingCacheLoader));
                        } else {
                            caffeineCacheManager.registerCustomCache(name, caffeine.build());
                        }
                    } else {
                        if (canLoad.contains(name)) {
                            caffeineCacheManager.registerCustomCache(name, defaultCaffeine.build(loadingCacheLoader));
                        } else {
                            caffeineCacheManager.registerCustomCache(name, defaultCaffeine.build());
                        }
                    }
                }
            }
        }
        return caffeineCacheManager.getCache(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return caffeineCacheManager.getCacheNames();
    }
}
