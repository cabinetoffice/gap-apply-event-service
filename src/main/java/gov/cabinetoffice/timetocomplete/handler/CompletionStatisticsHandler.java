package gov.cabinetoffice.timetocomplete.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.eventservice.handler.EventServiceHandler;
import gov.cabinetoffice.eventservice.service.SecretsManagerService;
import gov.cabinetoffice.shared.config.ObjectMapperConfig;
import gov.cabinetoffice.shared.config.SecretsManagerConfig;
import gov.cabinetoffice.shared.exceptions.MessageProcessingException;
import gov.cabinetoffice.shared.repository.EventLogRepository;
import gov.cabinetoffice.timetocomplete.repository.CompletionStatisticsRepository;
import gov.cabinetoffice.timetocomplete.service.CompletionStatisticsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;

@RequiredArgsConstructor
@Service
public class CompletionStatisticsHandler implements RequestHandler<SQSEvent, Void> {

    private final Logger logger = LoggerFactory.getLogger(EventServiceHandler.class);

    private final SecretsManagerService secretsManagerService;

    private final CompletionStatisticsRepository completionStatisticsRepository;

    private final EventLogRepository eventLogRepository;

    private final CompletionStatisticsService completionStatisticsService;

    private final ObjectMapper objectMapper;

    private final Clock clock;

    public CompletionStatisticsHandler() {
        objectMapper = new ObjectMapperConfig().getObjectMapper();
        clock = Clock.systemDefaultZone();
        secretsManagerService = new SecretsManagerService(System.getenv("DB_CREDS_SECRET_ARN"), SecretsManagerConfig.getAwsSecretsManager(), objectMapper);
        eventLogRepository = new EventLogRepository(secretsManagerService, clock);
        completionStatisticsRepository = new CompletionStatisticsRepository(secretsManagerService, clock);
        completionStatisticsService = new CompletionStatisticsService(eventLogRepository, completionStatisticsRepository );
    }

    @Override
    public Void handleRequest(final SQSEvent message, final Context context) {

        try {
            message.getRecords().forEach(record -> {
                logger.debug("Message Body : {}", record.getBody());

                    completionStatisticsService.calculateCompletionStatistics();


            });

        }
        catch (Exception e) {
            logger.error("Could not process message", e);
            throw new MessageProcessingException("Could not process message : " + message.toString());
        }

        return null;
    }
}
