package se.plilja.loadingcache;

import java.lang.reflect.Method;
import java.util.Arrays;

class CacheKey {
    private final Object[] args;
    private final Object target;
    private final Method method;

    CacheKey(Object[] args, Object target, Method method) {
        this.args = args;
        this.target = target;
        this.method = method;
    }

    Object[] getArgs() {
        return args;
    }

    Object getTarget() {
        return target;
    }

    Method getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CacheKey)) return false;
        CacheKey cacheKey = (CacheKey) o;
        return Arrays.equals(args, cacheKey.args);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(args);
    }
}
