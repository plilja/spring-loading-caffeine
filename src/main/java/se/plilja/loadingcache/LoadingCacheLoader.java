package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class LoadingCacheLoader implements CacheLoader<Object, Object> {
    private final Map<Method, Function<CacheKey, Object>> loaders = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public Object load(@NonNull Object key) {
        if (!(key instanceof CacheKey)) {
            throw new IllegalArgumentException("Can only load values if key is CacheKey was " + key.getClass().getSimpleName());
        }
        CacheKey cacheKey = (CacheKey) key;
        Function<CacheKey, Object> loader = loaders.getOrDefault(cacheKey.getMethod(), (_key) -> {
            throw new IllegalArgumentException(String.format("Do not know how to load values for %s", cacheKey.getMethod().getName()));
        });
        return loader.apply(cacheKey);
    }

    void addLoader(Method method, Function<CacheKey, Object> loader) {
        loaders.put(method, loader);
    }
}
