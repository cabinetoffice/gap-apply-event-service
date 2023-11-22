package gov.cabinetoffice.eventservice.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.eventservice.service.EventLogService;
import gov.cabinetoffice.shared.entity.EventLog;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EventServiceHandler implements RequestHandler<SQSEvent, Void> {

    private final Logger logger = LoggerFactory.getLogger(EventServiceHandler.class);
    private final EventLogService eventLogService;
    private final ObjectMapper objectMapper;

    @Override
    public Void handleRequest(final SQSEvent message, final Context context) {

        try {
             message.getRecords().forEach(record -> {
                logger.debug("Message Body : {}", record.getBody());

                 try {
                     eventLogService.save(objectMapper.readValue(record.getBody(), EventLog.class));
                 } catch (JsonProcessingException e) {
                     throw new RuntimeException(e);
                 }
             });

        }
        catch (Exception e) {
            logger.error("Could not process message", e);
            throw new RuntimeException(e);
        }

        return null;
    }

}
