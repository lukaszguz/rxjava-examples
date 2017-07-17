package pl.guz.rxjavaexamples.domain.model.requestresponse;

import lombok.Data;

import java.util.UUID;

@Data
class Communication {
    private UUID id;
    private UUID correlationId;
    private CommunicationType communicationType;
    private String message;
    private String response;

    public Communication(UUID id, UUID correlationId, CommunicationType communicationType, String message) {
        this.id = id;
        this.correlationId = correlationId;
        this.communicationType = communicationType;
        this.message = message;
        this.response = "";
    }
}
