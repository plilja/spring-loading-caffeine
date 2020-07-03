package se.plilja;

import org.springframework.stereotype.Service;
import se.plilja.loadingcache.LoadingCacheable;

import java.util.concurrent.atomic.AtomicInteger;

@Service
class HelloService {
    private AtomicInteger invocationCounter = new AtomicInteger(0);

    @LoadingCacheable("hello")
    String hello() {
        int invocation = invocationCounter.incrementAndGet();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return String.format("Hello World %d\n", invocation);
    }
}
