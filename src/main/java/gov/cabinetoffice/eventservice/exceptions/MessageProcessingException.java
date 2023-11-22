package gov.cabinetoffice.eventservice.exceptions;

public class MessageProcessingException extends RuntimeException {

    public MessageProcessingException(String message) {
        super(message);
    }
}
