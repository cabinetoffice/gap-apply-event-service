package gov.cabinetoffice.eventservice.handler;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.ObjectMapperConfig;
import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

/**
 * This class exists just to test locally.
 * You'll need to set the following as env variables before running:
 *
 * DB_CREDS_SECRET_ARN=[secret arn for sandbox db creds]
 * AWS_ACCESS_KEY_ID=[your own access key id]
 * AWS_SECRET_ACCESS_KEY=[your own secret access key]
 *
 * Then uncomment the @Test annotation
 *
 */
@ExtendWith(MockitoExtension.class)
class EventServiceHandlerIntegrationTest {

    private EventServiceHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapperConfig().getObjectMapper();
        handler = new EventServiceHandler();
    }

    //@Test
    void handleRequest() throws JsonProcessingException {
        EventLog incomingEventLog = EventLog.builder()
                .objectId("1")
                .eventType(EventType.ADVERT_CREATED)
                .fundingOrganisationId(2L)
                .userSub("USER_SUB")
                .sessionId("SESSION_ID")
                .objectType(ObjectType.SUBMISSION)
                .timestamp(Instant.now())
                .build();

        SQSEvent event = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();

        message.setBody(objectMapper.writeValueAsString(incomingEventLog));
        event.setRecords(List.of(message));

        handler.handleRequest(event, null);

    }
}