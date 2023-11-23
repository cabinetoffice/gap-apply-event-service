package gov.cabinetoffice.shared.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.context.annotation.Bean;

@Data
@Builder
public class DatabaseCredentialsSecret {

    private String username;
    private String password;
    private String engine;
    private String host;
    private Integer port;
    private String dbname;
    private String dbInstanceIdentifier;

}
