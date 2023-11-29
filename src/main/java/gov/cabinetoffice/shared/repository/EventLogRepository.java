package gov.cabinetoffice.shared.repository;

import gov.cabinetoffice.eventservice.exceptions.DatabaseConnectionException;
import gov.cabinetoffice.eventservice.exceptions.DatabaseQueryException;
import gov.cabinetoffice.eventservice.service.SecretsManagerService;
import gov.cabinetoffice.shared.entity.EventLog;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Clock;
import java.time.Instant;

@Slf4j
@Repository
public class EventLogRepository {

    private final SecretsManagerService secretsManagerService;

    private final Clock clock;

    private static final Logger logger = LoggerFactory.getLogger(EventLogRepository.class);

    private static final String INSERT_EVENT_LOG = "INSERT INTO event_stream.event_log(\n" +
            "\t id, user_sub, funding_organisation_id, session_id, event_type, object_id, object_type, time_stamp, created)\n" +
            "\t VALUES (nextval('EVENT_LOG_ID_SEQ'), ?, ?, ?, ?, ?, ?, ?, ?);";

    public EventLogRepository(SecretsManagerService secretsManagerService, Clock clock) {
        this.secretsManagerService = secretsManagerService;
        this.clock = clock;
    }

    public void save(EventLog eventLog) {

        String jdbcUrl = buildDbUrl();
        try (Connection conn = DriverManager.getConnection(
                buildDbUrl(),
                secretsManagerService.getDatabaseCredentialsSecret().getUsername(),
                secretsManagerService.getDatabaseCredentialsSecret().getPassword())
        ) {
            validateConnection(jdbcUrl, conn);
            logger.debug("Database connection established");

            PreparedStatement insertStatement = conn.prepareStatement(INSERT_EVENT_LOG);

            insertStatement.setString(1, eventLog.getUserSub());
            insertStatement.setObject(2, eventLog.getFundingOrganisationId());
            insertStatement.setString(3, eventLog.getSessionId());
            insertStatement.setString(4, eventLog.getEventType().toString());
            insertStatement.setString(5, eventLog.getObjectId());
            insertStatement.setString(6, eventLog.getObjectType().toString());
            insertStatement.setTimestamp(7, Timestamp.from(eventLog.getTimestamp()));
            insertStatement.setTimestamp(8, Timestamp.from(Instant.now(clock)));

            logger.debug("About to execute insert statement. Saving object {}", eventLog);
            insertStatement.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseQueryException(e.getMessage());
        }

    }

    private void validateConnection(String jdbcUrl, Connection conn) throws SQLException {
        if (!conn.isValid(0)) {
            throw new DatabaseConnectionException("Unable to connect to " + jdbcUrl);
        }
    }

    private String buildDbUrl() {
        return new StringBuilder()
                .append("jdbc:postgresql://").append(secretsManagerService.getDatabaseCredentialsSecret().getHost())
                .append(":").append(secretsManagerService.getDatabaseCredentialsSecret().getPort())
                .append("/").append(secretsManagerService.getDatabaseCredentialsSecret().getDbname())
                .append("?currentSchema=event_stream")
                .toString();
    }

}
