/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */
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
