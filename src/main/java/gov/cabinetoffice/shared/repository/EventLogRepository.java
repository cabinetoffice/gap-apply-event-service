package gov.cabinetoffice.shared.repository;

import gov.cabinetoffice.eventservice.service.SecretsManagerService;
import gov.cabinetoffice.shared.dto.EventLogDto;
import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.exceptions.DatabaseQueryException;
import gov.cabinetoffice.shared.rowmapper.EventLogRowMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Repository
public class EventLogRepository extends BaseEventStreamRepository {

    private static final Logger logger = LoggerFactory.getLogger(EventLogRepository.class);


    private static final String GET_UNACTIONED_PUBLISHED_LOGS_SQL = "SELECT * FROM event_stream.event_log \n" +
            "where actioned = false and event_type in (?, ?, ?);";

    private static final String GET_EVENT_LOGS_BY_OBJECT_ID_SQL = "SELECT * FROM event_stream.event_log \n" +
            "where object_id = ? order by time_stamp asc;";

    private static final String INSERT_EVENT_LOG_SQL = "INSERT INTO event_stream.event_log(\n" +
            "\t id, user_sub, funding_organisation_id, session_id, event_type, object_id, object_type, time_stamp, created)\n" +
            "\t VALUES (nextval('EVENT_LOG_ID_SEQ'), ?, ?, ?, ?, ?, ?, ?, ?);";

    private static final String UPDATE_SET_ACTIONED_SQL = "UPDATE event_stream.event_log set actioned = true where object_id = ? ;";

    public EventLogRepository(SecretsManagerService secretsManagerService, Clock clock) {
        super(secretsManagerService, clock);
    }

    public List<EventLog> getUnactionedPublishedEventLogs() {

        String jdbcUrl = buildDbUrl();
        try (Connection conn = DriverManager.getConnection(
                buildDbUrl(),
                secretsManagerService.getDatabaseCredentialsSecret().getUsername(),
                secretsManagerService.getDatabaseCredentialsSecret().getPassword())
        ) {
            validateConnection(jdbcUrl, conn);
            logger.debug("Database connection established");

            PreparedStatement selectStatement = conn.prepareStatement(GET_UNACTIONED_PUBLISHED_LOGS_SQL);

            selectStatement.setString(1, EventType.ADVERT_PUBLISHED.toString());
            selectStatement.setString(2, EventType.APPLICATION_PUBLISHED.toString());
            selectStatement.setString(3, EventType.SUBMISSION_PUBLISHED.toString());

            ResultSet results = selectStatement.executeQuery();

            return EventLogRowMapper.map(results);


        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseQueryException(e.getMessage());
        }

    }

    public List<EventLog> getEventLogsByObjectId(String objectId) {

        String jdbcUrl = buildDbUrl();
        try (Connection conn = DriverManager.getConnection(
                buildDbUrl(),
                secretsManagerService.getDatabaseCredentialsSecret().getUsername(),
                secretsManagerService.getDatabaseCredentialsSecret().getPassword())
        ) {
            validateConnection(jdbcUrl, conn);
            logger.debug("Database connection established");

            PreparedStatement selectStatement = conn.prepareStatement(GET_EVENT_LOGS_BY_OBJECT_ID_SQL);

            selectStatement.setString(1, objectId);

            ResultSet results = selectStatement.executeQuery();

            return EventLogRowMapper.map(results);


        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseQueryException(e.getMessage());
        }

    }





    public void saveNewEventLog(EventLogDto eventLog) {

        String jdbcUrl = buildDbUrl();
        try (Connection conn = DriverManager.getConnection(
                buildDbUrl(),
                secretsManagerService.getDatabaseCredentialsSecret().getUsername(),
                secretsManagerService.getDatabaseCredentialsSecret().getPassword())
        ) {
            validateConnection(jdbcUrl, conn);
            logger.debug("Database connection established");

            PreparedStatement insertStatement = conn.prepareStatement(INSERT_EVENT_LOG_SQL);

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

    public void markLogsAsActionedForObjectId(String objectId) {

        String jdbcUrl = buildDbUrl();
        try (Connection conn = DriverManager.getConnection(
                buildDbUrl(),
                secretsManagerService.getDatabaseCredentialsSecret().getUsername(),
                secretsManagerService.getDatabaseCredentialsSecret().getPassword())
        ) {
            validateConnection(jdbcUrl, conn);
            logger.debug("Database connection established");

            PreparedStatement insertStatement = conn.prepareStatement(UPDATE_SET_ACTIONED_SQL);

            insertStatement.setString(1, objectId);

            logger.debug("About to execute update statement. Updating object {}", objectId);
            insertStatement.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseQueryException(e.getMessage());
        }

    }
}
