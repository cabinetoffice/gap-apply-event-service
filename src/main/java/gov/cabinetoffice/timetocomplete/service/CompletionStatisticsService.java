package gov.cabinetoffice.timetocomplete.service;

import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.repository.EventLogRepository;
import gov.cabinetoffice.timetocomplete.dto.CompletionStatisticsDto;
import gov.cabinetoffice.timetocomplete.exceptions.InvalidEventLogsException;
import gov.cabinetoffice.timetocomplete.repository.CompletionStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CompletionStatisticsService {

    private final EventLogRepository eventLogRepository;

    private final CompletionStatisticsRepository completionStatisticsRepository;

    private static final Logger logger = LoggerFactory.getLogger(CompletionStatisticsService.class);

    public void calculateCompletionStatistics () {

        List<EventLog> publishedEventLogs = eventLogRepository.getUnactionedPublishedEventLogs();

        publishedEventLogs.forEach(publishedObject -> {

            try{

            CompletionStatisticsDto completionStatisticsDto = CompletionStatisticsDto.builder()
                    .objectCompleted(publishedObject.getTimestamp())
                    .fundingOrganisationId(publishedObject.getFundingOrganisationId())
                    .userSub(publishedObject.getUserSub())
                    .objectId(publishedObject.getObjectId())
                    .objectType(publishedObject.getObjectType())
                    .build();


            AtomicLong timeWorkedOn = new AtomicLong(0);

            List<EventLog> eventLogsForObject =  eventLogRepository.getEventLogsByObjectId(publishedObject.getObjectId());

            completionStatisticsDto.setTotalAliveTime(getTotalTimeAliveForObject(eventLogsForObject));

            //for each session id, get time in session and add to timeWorkedOn

            eventLogsForObject.stream().map(EventLog::getSessionId).distinct()
                    .forEach(sessionId ->
                            timeWorkedOn.addAndGet(getTimeWorkedOnForEvents(eventLogsForObject.stream()
                                .filter(x -> x.getSessionId().equals(sessionId)).collect(Collectors.toList())))
                    );

            completionStatisticsDto.setTimeWorkedOn(timeWorkedOn.get());

            logger.debug("Time in total for object {} : {}ms, {} minutes",
                    completionStatisticsDto.getObjectId(),
                    completionStatisticsDto.getTimeWorkedOn(),
                    completionStatisticsDto.getTimeWorkedOnInMinutes());

            logger.debug("Saving new completion statistics : {}", completionStatisticsDto);
            completionStatisticsRepository.saveNewCompletionStatistic(completionStatisticsDto);
            eventLogRepository.markLogsAsActionedForObjectId(completionStatisticsDto.getObjectId());

            } catch(Exception e){
                logger.error("Error calculating statistics for object {}. Skipping.", publishedObject.getObjectId());
            }

        });

    }

    /**
     * We pass a subset of objects with the same sessionId and calculate the ime between the first and last events
     *
     * @param eventLogsForObject The event logs to calculate length of session
     *
     * @return Total time for the events in the session.
     */
    private long getTimeWorkedOnForEvents(List<EventLog> eventLogsForObject) {
        if(eventLogsForObject.stream().map(EventLog::getSessionId).distinct().count() > 1){
            throw new InvalidEventLogsException("Trying to process more than one session.");
        }

        EventLog lastEventInSession = eventLogsForObject.get(eventLogsForObject.size()-1);
        EventLog firstEventInSession = getEarliestEventLogForSessionId(eventLogsForObject, lastEventInSession.getSessionId());

        long timeInSession = ChronoUnit.MILLIS.between(firstEventInSession.getTimestamp(), lastEventInSession.getTimestamp());

        logger.debug("Time in session {} : {}ms {} minutes", lastEventInSession.getSessionId(), timeInSession, timeInSession/1000/60);

        return timeInSession;
    }

    private long getTotalTimeAliveForObject(List<EventLog> eventLogsForObject) {
        //Using the first and last objects in the list to work out time between created and published
        return ChronoUnit.MILLIS.between(eventLogsForObject.get(0).getTimestamp(),
                eventLogsForObject.get(eventLogsForObject.size()-1).getTimestamp());

    }

    private EventLog getLatestEventLogForSessionId(List<EventLog> eventLogs, String sessionId) {
        return eventLogs.stream()
                .filter(x -> x.getSessionId().equalsIgnoreCase(sessionId))
                .reduce(null, ( current, element) -> current == null
                        || element.getTimestamp().isAfter(current.getTimestamp()) ? element  : current);

    }

    private EventLog getEarliestEventLogForSessionId(List<EventLog> eventLogs, String sessionId) {
        return eventLogs.stream()
                .filter(x -> x.getSessionId().equalsIgnoreCase(sessionId))
                .reduce(null, ( current, element) -> current == null
                        || element.getTimestamp().isBefore(current.getTimestamp()) ? element  : current);

    }

}
