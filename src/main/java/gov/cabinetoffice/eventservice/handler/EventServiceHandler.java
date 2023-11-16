package gov.cabinetoffice.eventservice.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class EventServiceHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceHandler.class);


    @Override
    public SQSBatchResponse handleRequest(final SQSEvent event, final Context context) {
        try {
            logger.info("Hello World! Message Body : {}", event.getRecords().get(0).getBody());
        }
        catch (Exception e) {
            logger.error("Could not process message", e);
            throw new RuntimeException(e);
        }

        return new SQSBatchResponse();
    }

}
