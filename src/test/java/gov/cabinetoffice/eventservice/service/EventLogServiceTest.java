package gov.cabinetoffice.eventservice.service;

import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;
import gov.cabinetoffice.shared.repository.EventLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventLogServiceTest {

    @Mock
    private EventLogRepository eventLogRepository;

    private EventLogService eventLogService;

    @Captor
    private ArgumentCaptor<EventLog> eventLogCaptor;

    @BeforeEach
    void setUp() {
        eventLogService = new EventLogService(eventLogRepository);
    }

    @Test
    void save() {

        EventLog expectedEventLog = EventLog.builder()
                .userSub("USER_SUB")
                .sessionId("SESSION_ID")
                .objectId("1")
                .objectType(ObjectType.ADVERT)
                .eventType(EventType.ADVERT_CREATED)
                .fundingOrganisationId(0L)
                .build();

        eventLogService.save(expectedEventLog);
        verify(eventLogRepository).save(eventLogCaptor.capture());

        EventLog actualEventLog = eventLogCaptor.getValue();

        assertEquals(expectedEventLog, actualEventLog);

    }
}