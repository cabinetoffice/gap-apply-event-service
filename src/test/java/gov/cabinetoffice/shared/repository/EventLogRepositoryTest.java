package gov.cabinetoffice.shared.repository;

import gov.cabinetoffice.shared.exceptions.DatabaseConnectionException;
import gov.cabinetoffice.shared.exceptions.DatabaseQueryException;
import gov.cabinetoffice.eventservice.service.SecretsManagerService;
import gov.cabinetoffice.shared.dto.DatabaseCredentialsSecret;
import gov.cabinetoffice.shared.dto.EventLogDto;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventLogRepositoryTest {

    private final SecretsManagerService secretsManagerService = Mockito.mock(SecretsManagerService.class);

    private static MockedStatic<DriverManager> mockedDriverManager;

    private final Connection mockedConnection = Mockito.mock(Connection.class);
    private final PreparedStatement mockedPreparedStatement = Mockito.mock(PreparedStatement.class);

    private final Clock clock = Clock.fixed(Instant.parse("2023-01-01T12:00:00.00Z"), ZoneId.systemDefault());

    private EventLogRepository eventLogRepository;

    @BeforeEach
    void setUp() {
        mockedDriverManager = mockStatic(DriverManager.class);
        eventLogRepository = new EventLogRepository(secretsManagerService, clock);
    }

    @AfterEach
    void afterAll() {
        mockedDriverManager.close();
    }

    @Nested
    class save {

        @Test
        void successfulQuery() throws SQLException {
            EventLogDto expectedEventLog = EventLogDto.builder()
                    .objectId("1")
                    .eventType(EventType.ADVERT_CREATED)
                    .fundingOrganisationId(2L)
                    .userSub("USER_SUB")
                    .sessionId("SESSION_ID")
                    .objectType(ObjectType.SUBMISSION)
                    .timestamp(clock.instant())
                    .build();

            String dbHost = "host";
            int port = 1;
            String dbName = "dbName";
            DatabaseCredentialsSecret secret = DatabaseCredentialsSecret.builder()
                    .dbname(dbName)
                    .username("username")
                    .password("password")
                    .host(dbHost)
                    .port(port)
                    .build();
            String dbConnectionString = "jdbc:postgresql://" + dbHost
                    + ":" + port + "/" + dbName + "?currentSchema=event_stream" ;

            when(secretsManagerService.getDatabaseCredentialsSecret()).thenReturn(secret);

            mockedDriverManager.when(() ->
                            DriverManager.getConnection(eq(dbConnectionString), eq(secret.getUsername()), eq(secret.getPassword())))
                    .thenReturn(mockedConnection);

            when(mockedConnection.isValid(0)).thenReturn(true);
            when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);

            eventLogRepository.saveNewEventLog(expectedEventLog);

            //verifying query params are all correct and in the right position
            verify(mockedPreparedStatement).setString(eq(1), eq(expectedEventLog.getUserSub()));
            verify(mockedPreparedStatement).setObject(eq(2), eq(expectedEventLog.getFundingOrganisationId()));
            verify(mockedPreparedStatement).setString(eq(3), eq(expectedEventLog.getSessionId()));
            verify(mockedPreparedStatement).setString(eq(4), eq(expectedEventLog.getEventType().toString()));
            verify(mockedPreparedStatement).setString(eq(5), eq(expectedEventLog.getObjectId()));
            verify(mockedPreparedStatement).setString(eq(6), eq(expectedEventLog.getObjectType().toString()));
            verify(mockedPreparedStatement).setTimestamp(eq(7), eq(Timestamp.from(expectedEventLog.getTimestamp())));
            verify(mockedPreparedStatement).setTimestamp(eq(8), eq(Timestamp.from(Instant.now(clock))));


            verify(mockedPreparedStatement).executeUpdate();
        }


        @Test
        void cannotConnectToDatabase() throws SQLException {
            EventLogDto expectedEventLog = EventLogDto.builder()
                    .objectId("1")
                    .eventType(EventType.ADVERT_CREATED)
                    .fundingOrganisationId(2L)
                    .userSub("USER_SUB")
                    .sessionId("SESSION_ID")
                    .objectType(ObjectType.SUBMISSION)
                    .timestamp(clock.instant())
                    .build();

            String dbHost = "host";
            int port = 1;
            String dbName = "dbName";
            DatabaseCredentialsSecret secret = DatabaseCredentialsSecret.builder()
                    .dbname(dbName)
                    .username("username")
                    .password("password")
                    .host(dbHost)
                    .port(port)
                    .build();
            String dbConnectionString = "jdbc:postgresql://" + dbHost
                    + ":" + port + "/" + dbName + "?currentSchema=event_stream" ;

            when(secretsManagerService.getDatabaseCredentialsSecret()).thenReturn(secret);

            mockedDriverManager.when(() ->
                            DriverManager.getConnection(eq(dbConnectionString), eq(secret.getUsername()), eq(secret.getPassword())))
                    .thenReturn(mockedConnection);

            when(mockedConnection.isValid(0)).thenReturn(false);

            assertThrows(DatabaseConnectionException.class, () -> eventLogRepository.saveNewEventLog(expectedEventLog));
        }



        @Test
        void queryFails() throws SQLException {
            EventLogDto expectedEventLog = EventLogDto.builder()
                    .objectId("1")
                    .eventType(EventType.ADVERT_CREATED)
                    .fundingOrganisationId(2L)
                    .userSub("USER_SUB")
                    .sessionId("SESSION_ID")
                    .objectType(ObjectType.SUBMISSION)
                    .timestamp(clock.instant())
                    .build();

            String dbHost = "host";
            int port = 1;
            String dbName = "dbName";
            DatabaseCredentialsSecret secret = DatabaseCredentialsSecret.builder()
                    .dbname(dbName)
                    .username("username")
                    .password("password")
                    .host(dbHost)
                    .port(port)
                    .build();
            String dbConnectionString = "jdbc:postgresql://" + dbHost
                    + ":" + port + "/" + dbName + "?currentSchema=event_stream" ;

            when(secretsManagerService.getDatabaseCredentialsSecret()).thenReturn(secret);

            mockedDriverManager.when(() ->
                            DriverManager.getConnection(eq(dbConnectionString), eq(secret.getUsername()), eq(secret.getPassword())))
                    .thenReturn(mockedConnection);

            when(mockedConnection.isValid(0)).thenReturn(true);
            when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);

            when(mockedPreparedStatement.executeUpdate()).thenThrow(SQLException.class);

            assertThrows(DatabaseQueryException.class, () -> eventLogRepository.saveNewEventLog(expectedEventLog));

            //verifying query params are all correct and in the right position
            verify(mockedPreparedStatement).setString(eq(1), eq(expectedEventLog.getUserSub()));
            verify(mockedPreparedStatement).setObject(eq(2), eq(expectedEventLog.getFundingOrganisationId()));
            verify(mockedPreparedStatement).setString(eq(3), eq(expectedEventLog.getSessionId()));
            verify(mockedPreparedStatement).setString(eq(4), eq(expectedEventLog.getEventType().toString()));
            verify(mockedPreparedStatement).setString(eq(5), eq(expectedEventLog.getObjectId()));
            verify(mockedPreparedStatement).setString(eq(6), eq(expectedEventLog.getObjectType().toString()));
            verify(mockedPreparedStatement).setTimestamp(eq(7), eq(Timestamp.from(expectedEventLog.getTimestamp())));
            verify(mockedPreparedStatement).setTimestamp(eq(8), eq(Timestamp.from(Instant.now(clock))));

        }
    }

}