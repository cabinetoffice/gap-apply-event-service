package gov.cabinetoffice.shared.exceptions;

public class DatabaseQueryException extends RuntimeException {

    public DatabaseQueryException(String message) {
        super(message);
    }
}
