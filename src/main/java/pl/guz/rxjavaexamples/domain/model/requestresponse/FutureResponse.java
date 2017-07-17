package pl.guz.rxjavaexamples.domain.model.requestresponse;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.UUID;

@Getter
@Slf4j
@ToString
class FutureResponse extends SettableListenableFuture<Communication> {

    private Communication request;

    FutureResponse(Communication request) {
        this.request = request;
    }

    UUID getUuid() {
        return request.getCorrelationId();
    }

    boolean correlate(Communication response) {
        if (response.getCommunicationType() == CommunicationType.RESPONSE && request.getCorrelationId().equals(response.getCorrelationId())) {
            log.info("Before correlation: {}", request);
            request.setResponse(response.getMessage());
            log.info("After correlation: {}", request);
            return true;
        }
        return false;
    }
}
