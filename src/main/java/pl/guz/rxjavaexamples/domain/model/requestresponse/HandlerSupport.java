package pl.guz.rxjavaexamples.domain.model.requestresponse;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class HandlerSupport {

    final static ExecutorService pool = Executors.newFixedThreadPool(2);
    final static Map<UUID, FutureResponse> cache = new ConcurrentHashMap<>();
}
