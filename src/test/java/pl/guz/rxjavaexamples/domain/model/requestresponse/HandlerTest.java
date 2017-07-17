package pl.guz.rxjavaexamples.domain.model.requestresponse;

import io.reactivex.Observable;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
        )
                .blockingLast();

        // then
        assertFalse(HandlerSupport.cache.containsKey(correlationId));
        assertEquals("response", request.getResponse());
        assertEquals(res, response);
    }

}