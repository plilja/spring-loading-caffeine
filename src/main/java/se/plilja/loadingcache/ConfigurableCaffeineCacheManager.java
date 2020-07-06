package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class ConfigurableCaffeineCacheManager implements CacheManager {
    private final CaffeineCacheManager caffeineCacheManager;
    private final Map<String, Caffeine<Object, Object>> customizedCacheNames = new ConcurrentHashMap<>();
    private final Set<String> createdCustomizedCacheNames = ConcurrentHashMap.newKeySet();
    private final LoadingCacheLoader loadingCacheLoader = new LoadingCacheLoader();

    ConfigurableCaffeineCacheManager() {
        caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCacheLoader(loadingCacheLoader);
    }

    public void setCacheConfiguration(String name, Caffeine<Object, Object> caffeine) {
        customizedCacheNames.put(name, caffeine);
    }

    public void setCacheConfiguration(String name, CaffeineSpec caffeineSpec) {
        setCacheConfiguration(name, Caffeine.from(caffeineSpec));
    }

    void setLoadingCacheLoader(Method method, Function<CacheKey, Object> loader) {
        loadingCacheLoader.addLoader(method, loader);
    }

    @Override
    public Cache getCache(String name) {
        if (customizedCacheNames.containsKey(name) && !createdCustomizedCacheNames.contains(name)) {
            synchronized (this) {
                // double checked locking
                if (!createdCustomizedCacheNames.contains(name)) {
                    createdCustomizedCacheNames.add(name);
                    Caffeine<Object, Object> caffeine = customizedCacheNames.get(name);
                    caffeineCacheManager.registerCustomCache(name, caffeine.build(loadingCacheLoader));
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
