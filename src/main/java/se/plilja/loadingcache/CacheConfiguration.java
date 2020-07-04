package se.plilja.loadingcache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableCaching
@EnableAspectJAutoProxy
@Configuration
class CacheConfiguration {

    @Bean
    CaffeineCacheManager cacheManager(LoadingCacheLoader loadingCacheLoader) {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCacheLoader(loadingCacheLoader);
        return caffeineCacheManager;
    }

    @Bean("keyGenerator")
    KeyGenerator keyGenerator() {
        return (target, method, args) -> new CacheKey(args, target, method);
    }

}
