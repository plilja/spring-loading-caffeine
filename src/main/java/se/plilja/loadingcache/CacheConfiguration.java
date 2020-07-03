package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

@EnableCaching
@EnableAspectJAutoProxy
@Configuration
class CacheConfiguration {
    @Bean
    CaffeineCaches cacheManager(Environment environment) {
        CaffeineSpec defaultSpec = CaffeineSpec.parse("maximumSize=1000,refreshAfterWrite=5m,expireAfterWrite=10m,recordStats");
        return new CaffeineCaches(defaultSpec, environment);
    }
}
