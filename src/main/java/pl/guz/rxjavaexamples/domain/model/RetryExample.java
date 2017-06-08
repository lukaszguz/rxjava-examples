package pl.guz.rxjavaexamples.domain.model;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import lombok.extern.slf4j.Slf4j;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
public class RetryExample {

    public static Observable retryTimes(Observable<Throwable> throwable, int nTimes, Scheduler scheduler) {
        return throwable
                .zipWith(Observable.range(1, nTimes), (error, i) -> i)
                .flatMap(retryCount -> Observable.timer((long) Math.pow(2, retryCount), SECONDS, scheduler))
                .doOnSubscribe(disposable -> log.info("Start retry"));
    }

    public static Observable retryTimes(Observable<Throwable> throwable, int nTimes) {
        return throwable
                .zipWith(Observable.range(1, nTimes), (n, i) -> i)
                .flatMap(retryCount -> Observable.timer((long) Math.pow(2, retryCount), SECONDS));
    }
}
