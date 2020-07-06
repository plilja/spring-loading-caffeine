package se.plilja.loadingcache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableCaching
@EnableAspectJAutoProxy
@Configuration
public class LoadingCacheConfiguration {

    @Bean
    ConfigurableCaffeineCacheManager cacheManager() {
        return new ConfigurableCaffeineCacheManager();
    }

    @Bean("loadingCacheKeyGenerator")
    KeyGenerator loadingCacheKeyGenerator() {
        return (target, method, args) -> new CacheKey(args, target, method);
    }

}
