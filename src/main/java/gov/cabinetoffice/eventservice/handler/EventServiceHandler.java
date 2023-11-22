package gov.cabinetoffice.eventservice.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.ObjectMapperConfig;
import gov.cabinetoffice.eventservice.config.SecretsManagerConfig;
import gov.cabinetoffice.eventservice.exceptions.JsonException;
import gov.cabinetoffice.eventservice.exceptions.MessageProcessingException;
import gov.cabinetoffice.eventservice.service.EventLogService;
import gov.cabinetoffice.eventservice.service.SecretsManagerService;
import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;

@RequiredArgsConstructor
@Service
public class EventServiceHandler implements RequestHandler<SQSEvent, Void> {

    private final Logger logger = LoggerFactory.getLogger(EventServiceHandler.class);
    private final SecretsManagerService secretsManagerService;
    private final EventLogRepository eventLogRepository;
    private final EventLogService eventLogService;
    private final ObjectMapper objectMapper;
    private final Clock clock = Clock.systemUTC();

    public EventServiceHandler() {
        objectMapper = new ObjectMapperConfig().getObjectMapper();
        secretsManagerService = new SecretsManagerService(System.getenv("DB_CREDS_SECRET_ARN"), SecretsManagerConfig.getAwsSecretsManager(), objectMapper);
        eventLogRepository = new EventLogRepository(System.getProperty("database.schema"), secretsManagerService, clock);
        eventLogService = new EventLogService(eventLogRepository);
    }

    @Override
    public Void handleRequest(final SQSEvent message, final Context context) {

        try {
             message.getRecords().forEach(record -> {
                logger.debug("Message Body : {}", record.getBody());

                 try {
                     eventLogService.save(objectMapper.readValue(record.getBody(), EventLog.class));
                 } catch (JsonProcessingException e) {
                     throw new JsonException(e.getMessage());
                 }
             });

        }
        catch (Exception e) {
            logger.error("Could not process message", e);
            throw new MessageProcessingException("Could not process message : " + message.toString());
        }

        return null;
    }

}
