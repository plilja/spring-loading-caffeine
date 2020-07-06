package se.plilja.loadingcache;

import com.github.benmanes.caffeine.cache.CaffeineSpec;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

@EnableCaching
@EnableAspectJAutoProxy
@Configuration
public class LoadingCacheConfiguration {
    static final String LOADING_CACHE_KEY_GENERATOR = "loadingCacheKeyGenerator";

    @Bean
    ConfigurableCaffeineCacheManager cacheManager(Environment environment, MeterRegistry meterRegistry) {
        String defaultCacheConfiguration = environment.getProperty("caffeine.defaultSpec");
        CaffeineSpec defaultSpec = null;
        if (defaultCacheConfiguration != null) {
            defaultSpec = CaffeineSpec.parse(defaultCacheConfiguration);
        }
        ConfigurableCaffeineCacheManager cacheManager = new ConfigurableCaffeineCacheManager(defaultSpec, meterRegistry);
        for (PropertySource<?> propertySource : ((AbstractEnvironment) environment).getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource<?>) {
                for (String propertyName : ((EnumerablePropertySource<?>) propertySource).getPropertyNames()) {
                    if (propertyName.startsWith("caffeine.caches.")) {
                        String cacheName = propertyName.replace("caffeine.caches.", "");
                        String cacheConfiguration = environment.getProperty(propertyName);
                        CaffeineSpec caffeineSpec = CaffeineSpec.parse(cacheConfiguration);
                        cacheManager.setCacheConfiguration(cacheName, caffeineSpec);
                    }
                }
            }
        }
        return cacheManager;
    }

    @Bean(LOADING_CACHE_KEY_GENERATOR)
    KeyGenerator loadingCacheKeyGenerator() {
        return (target, method, args) -> new CacheKey(args, target, method);
    }

    @Bean
    LoadingCacheableAspect loadingCacheableAspect(ConfigurableCaffeineCacheManager cacheManager) {
        return new LoadingCacheableAspect(cacheManager);
    }

}
