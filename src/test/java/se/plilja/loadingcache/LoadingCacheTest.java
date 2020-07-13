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