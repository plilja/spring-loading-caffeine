package se.plilja;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class HelloController {
    private final HelloService helloService;

    HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/hello/{name}")
    String hello(@PathVariable String name) {
        return helloService.hello(name);
    }

    @DeleteMapping("/hello/{name}")
    void evictHello(@PathVariable String name) {
        helloService.evict(name);
    }

}
