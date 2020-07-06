package se.plilja;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
class HelloService {
    private AtomicInteger invocationCounter = new AtomicInteger(0);

    @Cacheable(value = "hello", keyGenerator = "loadingCacheKeyGenerator")
    public String hello(String name) {
        int invocation = invocationCounter.incrementAndGet();
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
