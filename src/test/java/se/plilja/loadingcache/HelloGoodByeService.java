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

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
class HelloGoodByeService {
    private Map<String, Integer> helloInvocationCounters = new ConcurrentHashMap<>();
    private Map<String, Integer> goodByeInvocationCounters = new ConcurrentHashMap<>();
    private final CacheManager cacheManager;

    HelloGoodByeService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    void reset() {
        helloInvocationCounters = new ConcurrentHashMap<>();
        goodByeInvocationCounters = new ConcurrentHashMap<>();
        for (String cacheName : cacheManager.getCacheNames()) {
            cacheManager.getCache(cacheName).clear();
        }
    }

    @Cacheable(value = "hello", keyGenerator = "loadingCacheKeyGenerator")
    public String hello(String name) {
        int invocation = helloInvocationCounters.merge(name, 1, Integer::sum);
        Delay.delay(20);
        return String.format("Hello %s %d", name, invocation);
    }

    @CacheEvict(value = "hello", keyGenerator = "loadingCacheKeyGenerator")
    public void evictHello(String name) {

    }

    @Cacheable(value = "goodBye", keyGenerator = "loadingCacheKeyGenerator")
    public String goodBye(String name) {
        int invocation = goodByeInvocationCounters.merge(name, 1, Integer::sum);
        Delay.delay(20);
        return String.format("Good bye %s %d", name, invocation);
    }

    @CacheEvict(value = "goodBye", keyGenerator = "loadingCacheKeyGenerator")
    public void evictGoodBye(String name) {

    }
}
