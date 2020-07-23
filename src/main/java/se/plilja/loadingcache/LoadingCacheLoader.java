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

import com.github.benmanes.caffeine.cache.CacheLoader;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

class LoadingCacheLoader implements CacheLoader<Object, Object> {
    private final Map<Method, Function<CacheKey, Object>> loaders = new ConcurrentHashMap<>();

    @Override
    public Object load(Object key) {
        if (!(key instanceof CacheKey)) {
            throw new IllegalArgumentException("Can only load values if key is of type CacheKey key was of type " + key.getClass().getSimpleName());
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
