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
