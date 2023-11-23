package gov.cabinetoffice.eventservice.exceptions;

public class DatabaseQueryException extends RuntimeException {

    public DatabaseQueryException(String message) {
        super(message);
    }
}
