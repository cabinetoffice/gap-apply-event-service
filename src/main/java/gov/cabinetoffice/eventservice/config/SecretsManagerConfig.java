package gov.cabinetoffice.eventservice.config;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import org.springframework.context.annotation.Bean;


public class SecretsManagerConfig {

    @Bean
    public static AWSSecretsManager getAwsSecretsManager(){
        return AWSSecretsManagerClientBuilder.standard()
                .withRegion("eu-west-2")
                .build();
    }

}
