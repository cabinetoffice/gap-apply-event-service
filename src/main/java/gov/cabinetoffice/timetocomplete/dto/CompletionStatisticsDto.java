package gov.cabinetoffice.timetocomplete.dto;

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
public class CompletionStatisticsDto {

    String userSub;
    Long fundingOrganisationId;
    String objectId;
    ObjectType objectType;
    long totalAliveTime;
    long timeWorkedOn;
    Instant objectCompleted;
    Instant created;

    public long getTimeWorkedOnInMinutes (){
        return timeWorkedOn / 1000 / 60;
    }


}
