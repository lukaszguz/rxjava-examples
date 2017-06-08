package pl.guz.rxjavaexamples.domain.model;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class RetryExampleTest {

    private static AtomicInteger counter = new AtomicInteger();

    @Before
    public void before() {
        counter = new AtomicInteger();
    }

    @Test
    public void retryOneTimeTest() throws InterruptedException {
        int nTimes = 1;
        TestScheduler testScheduler = new TestScheduler();

        TestObserver<String> testObserver = Observable.fromCallable(() -> function(nTimes))
                .retryWhen(error -> RetryExample.retryTimes(error, nTimes, testScheduler))
                .test();

        testScheduler.advanceTimeBy(1999, MILLISECONDS);
        testObserver
                .assertNoValues()
                .assertNoErrors();

        testScheduler.advanceTimeBy(1, MILLISECONDS);
        testObserver
                .await()
                .assertValue("A");
    }

    @Test
    public void retryTwoTimesTest() throws InterruptedException {
        int nTimes = 2;
        TestScheduler testScheduler = new TestScheduler();

        TestObserver<String> testObserver = Observable.fromCallable(() -> function(nTimes))
                .retryWhen(error -> RetryExample.retryTimes(error, nTimes, testScheduler))
                .test();

        testScheduler.advanceTimeBy(3999, MILLISECONDS);
        testObserver
                .assertNoValues()
                .assertNoErrors();

        testScheduler.advanceTimeBy(4000, MILLISECONDS);
        testObserver
                .await()
                .assertValue("A");
    }

    @Test
    public void retryThreeTimesTest() throws InterruptedException {
        int nTimes = 2;
        TestScheduler testScheduler = new TestScheduler();

        TestObserver<String> testObserver = Observable.fromCallable(() -> function(nTimes))
                .retryWhen(error -> RetryExample.retryTimes(error, nTimes - 1, testScheduler))
                .defaultIfEmpty("Z")
                .test();

        testScheduler.advanceTimeBy(1999, MILLISECONDS);
        testObserver
                .assertNoValues()
                .assertNoErrors();

        testScheduler.advanceTimeBy(2000, MILLISECONDS);
        testObserver
                .await()
                .assertValue("Z");
    }


    private String function(int nTimes) {
        if (counter.get() < nTimes) {
            log.info("Increment and throw exception before: {} - nTimes: {}", counter.get(), nTimes);
            counter.incrementAndGet();
            log.info("Increment and throw exception after: {} - nTimes: {}", counter.get(), nTimes);
            throw new RuntimeException();
        } else if (counter.get() == nTimes) {
            log.info("Return value A");
            return "A";
        } else {
            return null;
        }
    }

}