package gov.cabinetoffice.eventservice.handler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.eventservice.service.EventLogService;
import gov.cabinetoffice.shared.config.ObjectMapperConfig;
import gov.cabinetoffice.shared.dto.EventLogDto;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;
import gov.cabinetoffice.shared.exceptions.DatabaseQueryException;
import gov.cabinetoffice.shared.exceptions.MessageProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceHandlerTest {

    private final EventLogService eventLogService = Mockito.mock(EventLogService.class);

    private ObjectMapper objectMapper;

    private final ObjectMapper mockedObjectMapper = Mockito.mock(ObjectMapper.class);

    private final Clock clock = Clock.fixed(Instant.parse("2023-01-01T12:00:00.00Z"), ZoneId.systemDefault());

    private EventServiceHandler eventServiceHandler;

    @Captor
    private ArgumentCaptor<EventLogDto> eventLogArgumentCaptor;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapperConfig().getObjectMapper();
        eventServiceHandler = new EventServiceHandler(null
                , null, eventLogService, objectMapper, clock);
    }
    @Nested
    class handleRequest {
        @Test
        void successful_request() throws JsonProcessingException {

            EventLogDto incomingEventLog = EventLogDto.builder()
                    .objectId("1")
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

            EventLogDto actualEventLog = eventLogArgumentCaptor.getValue();

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

            EventLogDto incomingEventLog = EventLogDto.builder()
                    .objectId("1")
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

            doThrow(new DatabaseQueryException("This was a horrible exception and you're lucky we caught it.")).when(eventLogService).save(any(EventLogDto.class));

            MessageProcessingException thrownException = assertThrows(MessageProcessingException.class,
                    () -> eventServiceHandler.handleRequest(event, null));

            assertEquals("Could not process message : " + event, thrownException.getMessage());

        }

        @Test
        void jsonProcessingException() throws JsonProcessingException {
            //This one has a mocked objectMapper so that we can force an exception
            eventServiceHandler = new EventServiceHandler(null, null, eventLogService, mockedObjectMapper, clock);

            EventLogDto incomingEventLog = EventLogDto.builder()
                    .objectId("1")
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

            when(mockedObjectMapper.readValue(anyString(), eq(EventLogDto.class))).thenThrow(JsonProcessingException.class);

            MessageProcessingException thrownException = assertThrows(MessageProcessingException.class,
                    () -> eventServiceHandler.handleRequest(event, null));

            assertEquals("Could not process message : " + event, thrownException.getMessage());


        }


    }

}