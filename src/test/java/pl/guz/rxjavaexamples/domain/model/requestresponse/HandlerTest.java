package pl.guz.rxjavaexamples.domain.model.requestresponse;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@Slf4j
public class HandlerTest {

    private Handler handler = new Handler();

    @Test
    public void should_add_request() throws InterruptedException {
        // given
        UUID requestUuid = UUID.randomUUID();
        UUID responseUuid = UUID.randomUUID();
        UUID correlationId = UUID.randomUUID();
        Communication request = new Communication(requestUuid, correlationId, CommunicationType.REQUEST, "firstRequest");
        Communication response = new Communication(responseUuid, correlationId, CommunicationType.RESPONSE, "response");

        // when
        Communication res = Observable.merge(
                handler.handle(request),
                Observable.timer(50, TimeUnit.MILLISECONDS).flatMap(aLong -> handler.handle(response))
        ).blockingLast();

        // then
        assertFalse(HandlerSupport.cache.containsKey(correlationId));
        assertEquals("response", request.getResponse());
        assertEquals(res, response);
    }


    @Test
    public void test1() throws InterruptedException {
        // given:
        Dummy dummy = new Dummy(log);
        Scheduler scheduler = Schedulers.from(Executors.newFixedThreadPool(2, new CustomizableThreadFactory("main-scheduler-")));

        // when:
//        Observable.defer(() -> Observable.fromFuture(dummy.doSomething(1, 1000)))
//                .doOnSubscribe(disposable -> log.info("Subscribe 1"))
//                .doOnTerminate(() -> log.info("doOnTerminate 1"))
//                .doOnNext(x -> log.info("doOnNext: {}", x))
//                .subscribeOn(scheduler)
//                .subscribe();
//
//        Observable.defer(() -> Observable.fromFuture(dummy.doSomething(2, 500)))
//                .doOnSubscribe(disposable -> log.info("Subscribe 2"))
//                .doOnTerminate(() -> log.info("doOnTerminate 2"))
//                .doOnNext(x -> log.info("doOnNext: {}", x))
//                .subscribeOn(scheduler)
//                .subscribe();

        Observable.merge(

                Observable.defer(() -> Observable.fromFuture(dummy.doSomething(1, 1000))
                        .doOnSubscribe(disposable -> log.info("Subscribe 1"))
                        .doOnTerminate(() -> log.info("doOnTerminate 1")))
                        .subscribeOn(scheduler)
                ,

                Observable.defer(() -> Observable.fromFuture(dummy.doSomething(2, 200))
                        .doOnSubscribe(disposable -> log.info("Subscribe 2"))
                        .doOnTerminate(() -> log.info("doOnTerminate 2")))
                        .subscribeOn(scheduler),

                Observable.defer(() -> Observable.fromFuture(dummy.doSomething(3, 200))
                        .doOnSubscribe(disposable -> log.info("Subscribe 3"))
                        .doOnTerminate(() -> log.info("doOnTerminate 3")))
                        .subscribeOn(scheduler)

        )
                .doOnNext(x -> log.info("doOnNext: {}", x))
                .subscribe();

        // then:
        TimeUnit.SECONDS.sleep(2);
    }

    @Value
    class Dummy {

        private Logger log;
        private ExecutorService executor = Executors.newFixedThreadPool(2,new CustomizableThreadFactory("dummy-thread-"));

        @SneakyThrows
        Future<String> doSomething(int id, long sleep) {
            return executor.submit(() -> {
                log.info("Start sleep - {}", id);
                TimeUnit.MILLISECONDS.sleep(sleep);
                log.info("End sleeping- {}", id);
                return "Value " + id;
            });
        }
    }
}