package se.plilja;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
class HelloService {
    private Map<String, Integer> invocationCounters = new ConcurrentHashMap<>();

    @Cacheable(value = "hello", keyGenerator = "loadingCacheKeyGenerator")
    public String hello(String name) {
        int invocation = invocationCounters.merge(name, 1, Integer::sum);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return String.format("Hello %s, invocation count=%d\n", name, invocation);
    }

    @CacheEvict(value = "hello", keyGenerator = "loadingCacheKeyGenerator")
    public void evict(String name) {

    }
}
