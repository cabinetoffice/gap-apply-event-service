package gov.cabinetoffice.shared.dto;

import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLogDto {

    String userSub;
    Long fundingOrganisationId;
    String sessionId;
    EventType eventType;
    String objectId;
    ObjectType objectType;
    Instant timestamp;
}
