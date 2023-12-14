package gov.cabinetoffice.timetocomplete.service;

import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;
import gov.cabinetoffice.shared.repository.EventLogRepository;
import gov.cabinetoffice.timetocomplete.dto.CompletionStatisticsDto;
import gov.cabinetoffice.timetocomplete.repository.CompletionStatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompletionStatisticsServiceTest {



    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private CompletionStatisticsRepository completionStatisticsRepository;

    private CompletionStatisticsService completionStatisticsService;

    @Captor
    private ArgumentCaptor<CompletionStatisticsDto> completionStatisticsArgumentCaptor;

    @BeforeEach
    void setUp() {

        completionStatisticsService = new CompletionStatisticsService(eventLogRepository, completionStatisticsRepository);

    }

    @Nested
    class doWork {

        /**
         * We're going to have 9 events, 3 in each session.
         * Timings will be hard coded so we can force the total_time_worked_on to be exact.
         */
        @Test
        public void success_with_1_object_across_3_sessions() {

            String objectId = "OBJECT_ID";
            Instant timestamp = Instant.now();
            ObjectType objectType = ObjectType.ADVERT;
            String sessionIdFirst = "SessionId_First";
            String sessionIdSecond = "SessionId_Second";
            String sessionIdThird = "SessionId_Third";
            EventType eventType = EventType.ADVERT_PUBLISHED;
            Long fundingOrgId = 1L;
            String userSub = "USER_SUB";


            List<String> publishedEventLogs = List.of(objectId);
            //User has logged in and created an advert
            EventLog event1 = generateEvent(timestamp.minus(40, ChronoUnit.MINUTES),
                    sessionIdFirst, objectId, objectType, EventType.ADVERT_CREATED, userSub, fundingOrgId);
            //They've updated the advert
            EventLog event2 = generateEvent(timestamp.minus(35, ChronoUnit.MINUTES),
                    sessionIdFirst, objectId, objectType, EventType.ADVERT_UPDATED, userSub, fundingOrgId);
            //They've updated the advert, then they log out
            EventLog event3 = generateEvent(timestamp.minus(30, ChronoUnit.MINUTES),
                    sessionIdFirst, objectId, objectType, EventType.ADVERT_UPDATED, userSub, fundingOrgId);
            //User has logged in then updated the advert
            EventLog event4 = generateEvent(timestamp.minus(25, ChronoUnit.MINUTES),
                    sessionIdSecond, objectId, objectType, EventType.ADVERT_UPDATED, userSub, fundingOrgId);
            //They've updated the advert
            EventLog event5 = generateEvent(timestamp.minus(20, ChronoUnit.MINUTES),
                    sessionIdSecond, objectId, objectType, EventType.ADVERT_UPDATED, userSub, fundingOrgId);
            //They've updated the advert, then they log out
            EventLog event6 = generateEvent(timestamp.minus(15, ChronoUnit.MINUTES),
                    sessionIdSecond, objectId, objectType, EventType.ADVERT_UPDATED, userSub, fundingOrgId);
            //User has logged in then updated the advert
            EventLog event7 = generateEvent(timestamp.minus(10, ChronoUnit.MINUTES),
                    sessionIdThird, objectId, objectType, EventType.ADVERT_UPDATED, userSub, fundingOrgId);
            //They've updated the advert
            EventLog event8 = generateEvent(timestamp.minus(5, ChronoUnit.MINUTES),
                    sessionIdThird, objectId, objectType, EventType.ADVERT_UPDATED, userSub, fundingOrgId);
            //They've published the advert
            EventLog event9 = generateEvent(timestamp,
                    sessionIdThird, objectId, objectType, EventType.ADVERT_PUBLISHED, userSub, fundingOrgId);

            List<EventLog> logsForObject = List.of(event1, event2, event3, event4,
                    event5, event6, event7 ,event8, event9);

            when(eventLogRepository.getUnactionedPublishedEventLogs()).thenReturn(publishedEventLogs);
            when(eventLogRepository.getEventLogsByObjectId(objectId)).thenReturn(logsForObject);

            completionStatisticsService.calculateCompletionStatistics();

            verify(completionStatisticsRepository).saveNewCompletionStatistic(completionStatisticsArgumentCaptor.capture());

            CompletionStatisticsDto actualCompletionStatistics = completionStatisticsArgumentCaptor.getValue();
            assertThat(actualCompletionStatistics.getTimeWorkedOn()).isEqualTo(1800000);



        }


        /**
         * We're going to have 9 events, 3 in each session.
         * Timings will be hard coded so we can force the total_time_worked_on to be exact.
         */
        @Test
        public void success_with_1_object() {

            String objectId1 = "OBJECT_ID_1";
            String objectId2 = "OBJECT_ID_2";
            String objectId3 = "OBJECT_ID_3";
            Instant timestamp1 = Instant.now();
            Instant timestamp2 = timestamp1.minus(1, ChronoUnit.HOURS);
            Instant timestamp3 = timestamp1.plus(1, ChronoUnit.HOURS);

            ObjectType objectType = ObjectType.ADVERT;
            String sessionIdFirst = "SessionId_First";
            String sessionIdSecond = "SessionId_Second";
            String sessionIdThird = "SessionId_Third";
            Long fundingOrgId = 1L;
            String userSub = "USER_SUB";


            List<String> publishedEventLogs = List.of(objectId1, objectId2, objectId3);

            //User has logged in and created an advert
            EventLog object1_event1 = generateEvent(timestamp1.minus(13, ChronoUnit.MINUTES),
                    sessionIdFirst, objectId1, objectType, EventType.ADVERT_CREATED, userSub, fundingOrgId);
            //They've updated the advert
            EventLog object1_event2 = generateEvent(timestamp1.minus(6, ChronoUnit.MINUTES),
                    sessionIdFirst, objectId1, objectType, EventType.ADVERT_UPDATED, userSub, fundingOrgId);
            //They've published the advert, then they log out
            EventLog object1_event3 = generateEvent(timestamp1,
                    sessionIdFirst, objectId1, objectType, EventType.ADVERT_PUBLISHED, userSub, fundingOrgId);


            //User has logged in and created an application
            EventLog object2_event1 = generateEvent(timestamp2.minus(26, ChronoUnit.MINUTES),
                    sessionIdSecond, objectId2, objectType, EventType.APPLICATION_CREATED, userSub, fundingOrgId);
            //They've updated the advert
            EventLog object2_event2 = generateEvent(timestamp2.minus(13, ChronoUnit.MINUTES),
                    sessionIdSecond, objectId2, objectType, EventType.APPLICATION_UPDATED, userSub, fundingOrgId);
            //They've published the advert, then they log out
            EventLog object2_event3 = generateEvent(timestamp2,
                    sessionIdSecond, objectId2, objectType, EventType.APPLICATION_PUBLISHED, userSub, fundingOrgId);


            //User has logged in and created an advert
            EventLog object3_event1 = generateEvent(timestamp3.minus(7, ChronoUnit.MINUTES),
                    sessionIdThird, objectId3, objectType, EventType.SUBMISSION_CREATED, userSub, fundingOrgId);
            //They've updated the advert
            EventLog object3_event2 = generateEvent(timestamp3.minus(1, ChronoUnit.MINUTES),
                    sessionIdThird, objectId3, objectType, EventType.SUBMISSION_UPDATED, userSub, fundingOrgId);
            //They've published the advert, then they log out
            EventLog object3_event3 = generateEvent(timestamp3,
                    sessionIdThird, objectId3, objectType, EventType.SUBMISSION_PUBLISHED, userSub, fundingOrgId);



            List<EventLog> logsForObject1 = List.of(object1_event1, object1_event2, object1_event3);
            List<EventLog> logsForObject2 = List.of(object2_event1, object2_event2, object2_event3);
            List<EventLog> logsForObject3 = List.of(object3_event1, object3_event2, object3_event3);

            when(eventLogRepository.getUnactionedPublishedEventLogs()).thenReturn(publishedEventLogs);
            when(eventLogRepository.getEventLogsByObjectId(objectId1)).thenReturn(logsForObject1);
            when(eventLogRepository.getEventLogsByObjectId(objectId2)).thenReturn(logsForObject2);
            when(eventLogRepository.getEventLogsByObjectId(objectId3)).thenReturn(logsForObject3);

            completionStatisticsService.calculateCompletionStatistics();

            verify(completionStatisticsRepository, times(3)).saveNewCompletionStatistic(completionStatisticsArgumentCaptor.capture());

            CompletionStatisticsDto object1_actualCompletionStatistics = completionStatisticsArgumentCaptor.getAllValues().get(0);
            assertThat(object1_actualCompletionStatistics.getTimeWorkedOn()).isEqualTo(780000);

            CompletionStatisticsDto object2_actualCompletionStatistics = completionStatisticsArgumentCaptor.getAllValues().get(1);
            assertThat(object2_actualCompletionStatistics.getTimeWorkedOn()).isEqualTo(1560000);

            CompletionStatisticsDto object3_actualCompletionStatistics = completionStatisticsArgumentCaptor.getAllValues().get(2);
            assertThat(object3_actualCompletionStatistics.getTimeWorkedOn()).isEqualTo(420000);

        }




        private EventLog generateEvent(Instant timestamp, String sessionId, String objectId, ObjectType objectType,
                                       EventType eventType, String userSub, Long fundingOrganisationId ){
            return EventLog.builder()
                    .objectId(objectId)
                    .timestamp(timestamp)
                    .created(timestamp)
                    .objectType(objectType)
                    .sessionId(sessionId)
                    .eventType(eventType)
                    .fundingOrganisationId(fundingOrganisationId)
                    .userSub(userSub)
                    .build();
        }



    }



}