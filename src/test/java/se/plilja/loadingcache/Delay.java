package se.plilja.loadingcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Delay {
    private static final Logger log = LoggerFactory.getLogger(Delay.class);

    static void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Caught interrupted exception", e);
        }
    }
}
