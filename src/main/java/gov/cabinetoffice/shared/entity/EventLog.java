package gov.cabinetoffice.shared.entity;

import com.google.gson.annotations.Expose;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventLog {

    String userSub;
    Long fundingOrganisationId;
    String sessionId;
    EventType eventType;
    Long objectId;
    ObjectType objectType;
    Instant timestamp;
}
