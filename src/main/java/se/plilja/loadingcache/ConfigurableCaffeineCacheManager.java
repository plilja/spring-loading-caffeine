package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class ConfigurableCaffeineCacheManager implements CacheManager {
    private final Optional<CaffeineSpec> defaultCaffeineSpecOptional;
    private final MeterRegistry meterRegistry;
    private final Map<String, CaffeineSpec> customizedCacheNames = new ConcurrentHashMap<>();
    private final LoadingCacheLoader loadingCacheLoader = new LoadingCacheLoader();
    private final Map<String, Cache> caches = new ConcurrentHashMap<>();
    private final Set<String> canLoad = ConcurrentHashMap.newKeySet();

    ConfigurableCaffeineCacheManager(CaffeineSpec defaultSpec, MeterRegistry meterRegistry) {
        this.defaultCaffeineSpecOptional = Optional.ofNullable(defaultSpec);
        this.meterRegistry = meterRegistry;
    }

    public void setCacheConfiguration(String name, CaffeineSpec caffeineSpec) {
        customizedCacheNames.put(name, caffeineSpec);
    }

    void setLoadingCacheLoader(List<String> caches, Method method, Function<CacheKey, Object> loader) {
        loadingCacheLoader.addLoader(method, loader);
        canLoad.addAll(caches);
    }

    @Override
    public Cache getCache(String name) {
        return caches.computeIfAbsent(name, (_name) -> {
            if (customizedCacheNames.containsKey(name)) {
                CaffeineSpec caffeineSpec = customizedCacheNames.get(name);
                return createCache(name, Optional.of(caffeineSpec));
            } else {
                return createCache(name, defaultCaffeineSpecOptional);
            }
        });
    }

    private CaffeineCache createCache(String name, Optional<CaffeineSpec> fromSpec) {
        com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache;
        Caffeine<Object, Object> caffeine = fromSpec.map(Caffeine::from).orElseGet(Caffeine::newBuilder);
        if (canLoad.contains(name)) {
            caffeineCache = caffeine.build(loadingCacheLoader);
        } else {
            caffeineCache = caffeine.build();
        }
        CaffeineCache springCache = new CaffeineCache(name, caffeineCache);
        if (fromSpec.map(spec -> spec.toParsableString().contains("recordStats")).orElse(false)) {
            CaffeineCacheMetrics.monitor(meterRegistry, caffeineCache, name, List.of());
        }
        return springCache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
}
