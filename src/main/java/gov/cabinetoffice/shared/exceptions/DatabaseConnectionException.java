package gov.cabinetoffice.shared.exceptions;

public class DatabaseConnectionException extends RuntimeException {

    public DatabaseConnectionException(String message) {
        super(message);
    }
}
