package pl.guz.rxjavaexamples.backpressure;

import io.reactivex.Emitter;
import io.reactivex.Flowable;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

public class EmitterTest {

    @Test
    public void should_emit_test() {
        brokenEmitter()
                .doOnNext(System.out::println)
                .blockingSubscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription subscription) {
                        subscription.request(3);
                    }

                    @Override
                    public void onNext(Integer integer) {
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Complete");
                    }
                });
    }

    @Test
    public void flatMap_broken_emitter_test() {
        brokenEmitter()
                .doOnNext(System.out::println)
                .rebatchRequests(2)
                .blockingSubscribe();
    }

    private Flowable<Integer> brokenEmitter() {
        return Flowable.generate(
                () -> new AtomicInteger(0),
                (AtomicInteger init, Emitter<Integer> emitter) -> {
                    int a = init.incrementAndGet();
                    if (a != 2) {
                        emitter.onNext(a);
                    } else {
                        System.out.println("Nothing emit!");
                    }
                },
                atomicInteger -> {
                }
        );
    }

}
