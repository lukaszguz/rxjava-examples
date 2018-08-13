package pl.guz.rxjavaexamples.empty;

import io.reactivex.Flowable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmptyExample {

    private static Logger logger = LoggerFactory.getLogger(EmptyExample.class);

    @Test
    public void empty_do_nothing() {
        Flowable<String> strings = Flowable.just("a", "2", "c")
                .doOnNext(c -> logger.info("First step: {}", c))
                .flatMap(this::emptyIfNumber)
                .map(String::toUpperCase)
                .doOnNext(c -> logger.info("Second step: {}", c));

        strings.test()
                .assertValues("A", "C");
    }

    private Flowable<String> emptyIfNumber(String potentialNumber) {
        try {
            new Integer(potentialNumber);
            return Flowable.empty();
        } catch (NumberFormatException e) {
            return Flowable.just(potentialNumber);
        }

    }
}
