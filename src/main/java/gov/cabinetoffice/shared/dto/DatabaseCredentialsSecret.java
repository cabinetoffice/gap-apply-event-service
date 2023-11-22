package gov.cabinetoffice.shared.dto;

import lombok.Data;

@Data
public class DatabaseCredentialsSecret {

    private String username;
    private String password;
    private String engine;
    private String host;
    private Integer port;
    private String dbname;
    private String dbInstanceIdentifier;

}
