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
