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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static se.plilja.loadingcache.Delay.delay;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoadingCacheTest {

    @LocalServerPort
    int port;

    @Autowired
    HelloGoodByeService helloGoodByeService;

    RestTemplate restTemplate = new RestTemplate();

    @AfterEach
    void tearDown() {
        helloGoodByeService.reset();
    }

    @Test
    void nonLoadingCache() {
        String result1 = restTemplate.getForObject("http://localhost:{port}/hello/World", String.class, port);
        assertEquals("Hello World 1", result1);
        String result2 = restTemplate.getForObject("http://localhost:{port}/hello/World", String.class, port);
        assertEquals("Hello World 1", result2);

        delay(1200); // Wait for cache entry to expire

        String result3 = restTemplate.getForObject("http://localhost:{port}/hello/World", String.class, port);
        assertEquals("Hello World 2", result3);
        String result4 = restTemplate.getForObject("http://localhost:{port}/hello/World", String.class, port);
        assertEquals("Hello World 2", result4);
    }

    @Test
    void loadingCache() {
        String result1 = restTemplate.getForObject("http://localhost:{port}/goodBye/World", String.class, port);
        assertEquals("Good bye World 1", result1);
        String result2 = restTemplate.getForObject("http://localhost:{port}/goodBye/World", String.class, port);
        assertEquals("Good bye World 1", result2);

        delay(1200); // Wait for refresh timer to expire

        String result3 = restTemplate.getForObject("http://localhost:{port}/goodBye/World", String.class, port);
        assertEquals("Good bye World 1", result3);
        Delay.delay(50);
        String result4 = restTemplate.getForObject("http://localhost:{port}/goodBye/World", String.class, port);
        assertEquals("Good bye World 2", result4);

        delay(2500); // Wait for cache entry to expire

        String result5 = restTemplate.getForObject("http://localhost:{port}/goodBye/World", String.class, port);
        assertEquals("Good bye World 3", result5);
    }

    @Test
    void cacheEvict() {
        String result1 = restTemplate.getForObject("http://localhost:{port}/goodBye/World", String.class, port);
        assertEquals("Good bye World 1", result1);

        restTemplate.delete("http://localhost:{port}/goodBye/World", port);

        String result2 = restTemplate.getForObject("http://localhost:{port}/goodBye/World", String.class, port);
        assertEquals("Good bye World 2", result2);
    }
}