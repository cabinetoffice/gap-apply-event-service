package gov.cabinetoffice.shared.rowmapper;

import gov.cabinetoffice.shared.entity.EventLog;
import gov.cabinetoffice.shared.enums.EventType;
import gov.cabinetoffice.shared.enums.ObjectType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventLogRowMapper  {

    public static List<EventLog> map(ResultSet resultSet) throws SQLException {

        List<EventLog> eventLogs = new ArrayList<>();

        while (resultSet.next()) {
            eventLogs.add(EventLog.builder()
                            .id(resultSet.getLong("id"))
                            .userSub(resultSet.getString("user_sub"))
                            .fundingOrganisationId((Long) resultSet.getObject("funding_organisation_id"))
                            .sessionId(resultSet.getString("session_id"))
                            .eventType(EventType.valueOf(resultSet.getString("event_type")))
                            .objectId(resultSet.getString("object_id"))
                            .objectType(ObjectType.valueOf(resultSet.getString("object_type")))
                            .timestamp(resultSet.getTimestamp("time_stamp").toInstant())
                            .created(resultSet.getTimestamp("created").toInstant())
                    .build());

        }

        return eventLogs;

    }
}
