/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */
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
