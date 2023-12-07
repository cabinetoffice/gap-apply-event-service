package gov.cabinetoffice.timetocomplete.repository;

import gov.cabinetoffice.eventservice.service.SecretsManagerService;
import gov.cabinetoffice.shared.exceptions.DatabaseQueryException;
import gov.cabinetoffice.shared.repository.BaseEventStreamRepository;
import gov.cabinetoffice.timetocomplete.dto.CompletionStatisticsDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Clock;
import java.time.Instant;

@Slf4j
@Repository
public class CompletionStatisticsRepository extends BaseEventStreamRepository {

    private static final Logger logger = LoggerFactory.getLogger(CompletionStatisticsRepository.class);

    public CompletionStatisticsRepository(SecretsManagerService secretsManagerService, Clock clock) {
        super(secretsManagerService, clock);
    }

    private static final String INSERT_COMPLETION_STATISTICS_SQL = "INSERT INTO COMPLETION_STATISTICS \n" +
            "( id, user_sub, funding_organisation_id, object_id, object_type, total_alive_time, time_worked_on, object_completed, created ) \n" +
            "values (nextval('COMPLETION_STATISTICS_ID_SEQ'),?,?,?,?,?,?,?,? );";


    public void saveNewCompletionStatistic(CompletionStatisticsDto completionStatistics) {

        String jdbcUrl = buildDbUrl();
        try (Connection conn = DriverManager.getConnection(
                buildDbUrl(),
                secretsManagerService.getDatabaseCredentialsSecret().getUsername(),
                secretsManagerService.getDatabaseCredentialsSecret().getPassword())) {
            validateConnection(jdbcUrl, conn);
            logger.debug("Database connection established");

            PreparedStatement insertStatement = conn.prepareStatement(INSERT_COMPLETION_STATISTICS_SQL);

            insertStatement.setString(1, completionStatistics.getUserSub());
            insertStatement.setObject(2, completionStatistics.getFundingOrganisationId());
            insertStatement.setString(3, completionStatistics.getObjectId());
            insertStatement.setString(4, completionStatistics.getObjectType().toString());
            insertStatement.setLong(5, completionStatistics.getTotalAliveTime());
            insertStatement.setLong(6, completionStatistics.getTimeWorkedOn());
            insertStatement.setTimestamp(7, Timestamp.from(completionStatistics.getObjectCompleted()));
            insertStatement.setTimestamp(8, Timestamp.from(Instant.now(clock)));

            logger.debug("About to execute insert statement. Saving object {}", completionStatistics);
            insertStatement.executeUpdate();

        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseQueryException(e.getMessage());
        }

    }




}
