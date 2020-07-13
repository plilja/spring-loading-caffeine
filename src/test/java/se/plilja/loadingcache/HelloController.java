package se.plilja.loadingcache;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
class HelloController {
    private final HelloGoodByeService helloGoodByeService;

    HelloController(HelloGoodByeService helloGoodByeService) {
        this.helloGoodByeService = helloGoodByeService;
    }

    @GetMapping("/hello/{name}")
    String hello(@PathVariable String name) {
        return helloGoodByeService.hello(name);
    }

    @DeleteMapping("/hello/{name}")
    void evictHello(@PathVariable String name) {
        helloGoodByeService.evictHello(name);
    }

    @GetMapping("/goodBye/{name}")
    String goodBye(@PathVariable String name) {
        return helloGoodByeService.goodBye(name);
    }

    @DeleteMapping("/goodBye/{name}")
    void evictGoodBye(@PathVariable String name) {
        helloGoodByeService.evictGoodBye(name);
    }
}
