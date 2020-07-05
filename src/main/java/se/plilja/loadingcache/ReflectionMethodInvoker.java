package se.plilja.loadingcache;

import java.lang.reflect.Method;

/**
 * Util class to invoke methods via reflection without the hazzle
 * of checked exceptions.
 */
final class ReflectionMethodInvoker {
    private ReflectionMethodInvoker() {
        // Should not be instantiated
    }

    static Object invoke(Object target, Method method, Object[] args) {
        try {
            if (!method.canAccess(target)) {
                method.setAccessible(true);
            }
            return method.invoke(target, args);
        } catch (Throwable throwable) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                throw new RuntimeException("Caught exception while calling method", throwable);
            }
        }
    }
}
