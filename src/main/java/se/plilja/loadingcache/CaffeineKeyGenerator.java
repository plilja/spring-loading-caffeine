package se.plilja.loadingcache;

import org.springframework.cache.interceptor.KeyGenerator;

import java.lang.reflect.Method;

public class CaffeineKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... args) {
        return new CacheKey(args, target, method);
    }
}
