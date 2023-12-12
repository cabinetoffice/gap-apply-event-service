package gov.cabinetoffice.shared.repository;

import gov.cabinetoffice.eventservice.service.SecretsManagerService;
import gov.cabinetoffice.shared.exceptions.DatabaseConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;

@Slf4j
@Repository
public class BaseEventStreamRepository {

    protected final SecretsManagerService secretsManagerService;

    protected final Clock clock;

    public BaseEventStreamRepository(SecretsManagerService secretsManagerService, Clock clock) {
        this.secretsManagerService = secretsManagerService;
        this.clock = clock;
    }

    protected void validateConnection(String jdbcUrl, Connection conn) throws SQLException {
        if (!conn.isValid(0)) {
            throw new DatabaseConnectionException("Unable to connect to " + jdbcUrl);
        }
    }

    protected String buildDbUrl() {
        return new StringBuilder()
                .append("jdbc:postgresql://")
                .append(secretsManagerService.getDatabaseCredentialsSecret().getHost())
                .append(":").append(secretsManagerService.getDatabaseCredentialsSecret().getPort())
                .append("/").append(secretsManagerService.getDatabaseCredentialsSecret().getDbname())
                .append("?currentSchema=event_stream")
                .toString();
    }
}
