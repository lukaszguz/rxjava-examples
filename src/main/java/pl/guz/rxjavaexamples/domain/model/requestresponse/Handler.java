package pl.guz.rxjavaexamples.domain.model.requestresponse;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
class Handler {

    Observable<Communication> handle(Communication communication) {
        return request(communication)
                .switchIfEmpty(response(communication))
                .subscribeOn(Schedulers.from(HandlerSupport.pool))
                .doOnSubscribe(x -> log.info("Start subscribe"));
    }

    private Observable<Communication> request(Communication communication) {
        return Observable.defer(() -> Observable.just(communication)
                .doOnNext(request -> log.info("Start request: {}", request))
                .filter(request -> Objects.equals(request.getCommunicationType(), CommunicationType.REQUEST))
                .map(FutureResponse::new)
                .doOnNext(futureResponse -> log.info("Add to cache {}", futureResponse))
                .doOnNext(futureResponse -> HandlerSupport.cache.put(futureResponse.getUuid(), futureResponse))
                .flatMap(futureResponse -> Observable.fromFuture(futureResponse, 200, TimeUnit.MILLISECONDS))
                .doOnNext(response -> log.info("Get response: {}", response))
        );
    }

    private Observable<Communication> response(Communication communication) {
        return Observable.defer(() -> Observable.just(communication)
                .doOnNext(response -> log.info("Start response: {}", response))
                .filter(response -> Objects.equals(response.getCommunicationType(), CommunicationType.RESPONSE))
                .doOnNext(this::correlation));
    }

    private void correlation(Communication response) {
        FutureResponse request = HandlerSupport.cache.get(response.getCorrelationId());
        log.info("Check correlations: {}", request);
        Optional.ofNullable(request)
                .map(futureResponse -> futureResponse.correlate(response))
                .filter(x -> x)
                .ifPresent(x -> {
                    request.set(response);
                    HandlerSupport.cache.remove(response.getCorrelationId());
                });
    }
}
