package pl.guz.rxjavaexamples.mulitcast;

import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class MulticastTest {

    private Logger logger = LoggerFactory.getLogger(MulticastTest.class);
    Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(2, new CustomizableThreadFactory("scheduler-")));

    @Test
    public void publishTest() {
        ConnectableFlowable<Integer> published = coldIntegerPublisher()
                .doOnSubscribe(x -> logger.info("Subscribe published"))
                .take(5)
                .publish();

        logger.info("After subscribers");

        published.subscribe(x -> logger.info("A: {}", x));
        published.subscribe(x -> logger.info("B: {}", x));
        published.connect();
    }

    @Test
    public void publishRefCount() throws InterruptedException {
        Flowable<Integer> lazy = coldIntegerPublisher()
                .doOnSubscribe(x -> logger.info("Subscribe"))
                .take(5)
                .publish()
                .refCount(); // == share()

        logger.info("After subscribers");

        lazy.subscribeOn(scheduler).subscribe(x -> logger.info("A: {}", x));
        lazy.subscribeOn(scheduler).subscribe(x -> logger.info("B: {}", x));

        lazy.test()
                .await()
                .assertComplete();
    }

    @Test
    public void share() throws InterruptedException {
        Flowable<Integer> lazy = coldIntegerPublisher()
                .doOnSubscribe(x -> logger.info("Subscribe"))
                .take(5)
                .share();

        logger.info("After subscribers");

        lazy.subscribeOn(scheduler).subscribe(x -> logger.info("A: {}", x));
        lazy.subscribeOn(scheduler).subscribe(x -> logger.info("B: {}", x));

        lazy.test()
                .await()
                .assertComplete();
    }

    @Test
    public void multicast() {
        Flowable<Integer> lazy = coldIntegerPublisher()
                .doOnSubscribe(x -> logger.info("Subscribe"))
                .take(200)
                .publish(integerFlowable -> {
                    Maybe<Integer> firstElement = integerFlowable.firstElement();
                    Maybe<Integer> lastElement = integerFlowable.lastElement();
                    return firstElement.zipWith(lastElement, (i1, i2) -> i1 + i2)
                            .toFlowable();
                })
                .doOnNext(x -> logger.info("x = {}", x));

        logger.info("After subscribers");
        lazy.test()
                .assertValue(201)
                .assertComplete();
    }


    private Flowable<Integer> coldIntegerPublisher() {
        return Flowable.generate(
                () -> new AtomicInteger(0),
                (AtomicInteger init, Emitter<Integer> emitter) -> emitter.onNext(init.incrementAndGet()),
                atomicInteger -> {
                }
        );
    }
}
