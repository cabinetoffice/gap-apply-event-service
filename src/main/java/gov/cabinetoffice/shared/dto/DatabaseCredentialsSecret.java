package gov.cabinetoffice.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseCredentialsSecret {

    private String username;
    private String password;
    private String engine;
    private String host;
    private Integer port;
    private String dbname;
    private String dbInstanceIdentifier;

}
