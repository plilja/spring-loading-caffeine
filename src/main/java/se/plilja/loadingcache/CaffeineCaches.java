package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

class CaffeineCaches implements CacheManager {
    private final ReentrantLock lock = new ReentrantLock();
    private final Map<String, CaffeineCache> caffeineCaches = new ConcurrentHashMap<>();
    private final CaffeineSpec defaultSpec;
    private final Environment environment;

    CaffeineCaches(CaffeineSpec defaultSpec, Environment environment) {
        this.defaultSpec = defaultSpec;
        this.environment = environment;
    }

    @Override
    public Cache getCache(String cacheName) {
        return caffeineCaches.get(cacheName);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(caffeineCaches.keySet());
    }

    CaffeineCache getOrCreateLoadingCache(String name, Function<Object, Object> loadingFunction) {
        CaffeineCache caffeineCache = caffeineCaches.get(name);
        if (caffeineCache == null) {
            // double checked locking
            lock.lock();
            try {
                if (caffeineCache == null) {
                    String cacheConfigurationPropertyOrNull = environment.getProperty(String.format("caches.%s", name));
                    Caffeine<Object, Object> caffeine;
                    if (cacheConfigurationPropertyOrNull == null) {
                        caffeine = Caffeine.from(defaultSpec);
                    } else {
                        caffeine = Caffeine.from(cacheConfigurationPropertyOrNull);
                        if (!cacheConfigurationPropertyOrNull.contains("expireAfterWrite")) {
                            throw new IllegalStateException("Must define expireAfterWrite");
                        }
                        if (!cacheConfigurationPropertyOrNull.contains("refreshAfterWrite")) {
                            throw new IllegalStateException("Must define refreshAfterWrite");
                        }
                    }
                    LoadingCache<Object, Object> cache = caffeine
                            .build(loadingFunction::apply);
                    caffeineCache = new CaffeineCache(name, cache, true);
                    caffeineCaches.put(name, caffeineCache);
                }
            } finally {
                lock.unlock();
            }
        }
        return caffeineCache;
    }
}
