package gov.cabinetoffice.shared.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Configuration("awsClientConfigurationProperties")
@ConfigurationProperties(prefix = "aws")
public class AwsClientConfig {

	private String secretKey;

	private String accessKeyId;

	private String region;

}
