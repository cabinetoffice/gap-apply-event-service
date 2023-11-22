package gov.cabinetoffice.eventservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@RequiredArgsConstructor
@Configuration
public class BeanConfig {

    private final AwsClientConfig awsClientConfig;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        return StaticCredentialsProvider
                .create(AwsBasicCredentials.create(awsClientConfig.getAccessKeyId(), awsClientConfig.getSecretKey()));
    }

    @Bean
    public Region region() {
        return Region.of(awsClientConfig.getRegion());
    }

}
