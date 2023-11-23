package gov.cabinetoffice.eventservice.handler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.ObjectMapperConfig;
import gov.cabinetoffice.eventservice.exceptions.DatabaseQueryException;
import gov.cabinetoffice.eventservice.exceptions.JsonException;
import gov.cabinetoffice.eventservice.exceptions.MessageProcessingException;
import gov.cabinetoffice.eventservice.service.EventLogService;
import gov.cabinetoffice.eventservice.service.SecretsManagerService;
import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;
import gov.cabinetoffice.shared.repository.EventLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceHandlerTest {

    private SecretsManagerService secretsManagerService;
    private EventLogRepository eventLogRepository;
    private final EventLogService eventLogService = Mockito.mock(EventLogService.class);

    private ObjectMapper objectMapper;

    private final ObjectMapper mockedObjectMapper = Mockito.mock(ObjectMapper.class);

    private final Clock clock = Clock.fixed(Instant.parse("2023-01-01T12:00:00.00Z"), ZoneId.systemDefault());

    private EventServiceHandler eventServiceHandler;

    @Captor
    private ArgumentCaptor<EventLog> eventLogArgumentCaptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapperConfig().getObjectMapper();
        eventServiceHandler = new EventServiceHandler(secretsManagerService, eventLogRepository, eventLogService, objectMapper, clock);
    }
    @Nested
    class handleRequest {
        @Test
        void successful_request() throws JsonProcessingException {

            EventLog incomingEventLog = EventLog.builder()
                    .objectId(1L)
                    .eventType(EventType.ADVERT_CREATED)
                    .fundingOrganisationId(2L)
                    .userSub("USER_SUB")
                    .sessionId("SESSION_ID")
                    .objectType(ObjectType.SUBMISSION)
                    .timestamp(clock.instant())
                    .build();

            SQSEvent event = new SQSEvent();
            SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();

            message.setBody(objectMapper.writeValueAsString(incomingEventLog));
            event.setRecords(List.of(message));

            eventServiceHandler.handleRequest(event, null);

            verify(eventLogService).save(eventLogArgumentCaptor.capture());

            EventLog actualEventLog = eventLogArgumentCaptor.getValue();

            assertEquals(incomingEventLog.getEventType(), actualEventLog.getEventType());
            assertEquals(incomingEventLog.getObjectId(), actualEventLog.getObjectId());
            assertEquals(incomingEventLog.getFundingOrganisationId(), actualEventLog.getFundingOrganisationId());
            assertEquals(incomingEventLog.getObjectType(), actualEventLog.getObjectType());
            assertEquals(incomingEventLog.getTimestamp(), actualEventLog.getTimestamp());
            assertEquals(incomingEventLog.getUserSub(), actualEventLog.getUserSub());
            assertEquals(incomingEventLog.getSessionId(), actualEventLog.getSessionId());


        }

        @Test
        void eventLogServiceThrowsException() throws JsonProcessingException {

            EventLog incomingEventLog = EventLog.builder()
                    .objectId(1L)
                    .eventType(EventType.ADVERT_CREATED)
                    .fundingOrganisationId(2L)
                    .userSub("USER_SUB")
                    .sessionId("SESSION_ID")
                    .objectType(ObjectType.SUBMISSION)
                    .timestamp(clock.instant())
                    .build();

            SQSEvent event = new SQSEvent();
            SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
            String messageBody = objectMapper.writeValueAsString(incomingEventLog);

            message.setBody(messageBody);
            event.setRecords(List.of(message));

            doThrow(new DatabaseQueryException("This was a horrible exception and you're lucky we caught it.")).when(eventLogService).save(any(EventLog.class));

            MessageProcessingException thrownException = assertThrows(MessageProcessingException.class,
                    () -> eventServiceHandler.handleRequest(event, null));

            assertEquals("Could not process message : " + event, thrownException.getMessage());

        }

        @Test
        void jsonProcessingException() throws JsonProcessingException {
            //This one has a mocked objectMapper so that we can force an exception
            eventServiceHandler = new EventServiceHandler(secretsManagerService, eventLogRepository, eventLogService, mockedObjectMapper, clock);

            EventLog incomingEventLog = EventLog.builder()
                    .objectId(1L)
                    .eventType(EventType.ADVERT_CREATED)
                    .fundingOrganisationId(2L)
                    .userSub("USER_SUB")
                    .sessionId("SESSION_ID")
                    .objectType(ObjectType.SUBMISSION)
                    .timestamp(clock.instant())
                    .build();

            SQSEvent event = new SQSEvent();
            SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
            String messageBody = objectMapper.writeValueAsString(incomingEventLog);

            message.setBody(messageBody);
            event.setRecords(List.of(message));

            when(mockedObjectMapper.readValue(anyString(), eq(EventLog.class))).thenThrow(JsonProcessingException.class);

            MessageProcessingException thrownException = assertThrows(MessageProcessingException.class,
                    () -> eventServiceHandler.handleRequest(event, null));

            assertEquals("Could not process message : " + event, thrownException.getMessage());


        }


    }

}