package gov.cabinetoffice.eventservice.service;

import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.repository.EventLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventLogService {

    private final EventLogRepository eventLogRepository;

    public void save(EventLog eventLog){
        eventLogRepository.save(eventLog);
    }

}
