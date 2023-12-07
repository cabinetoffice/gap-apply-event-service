package gov.cabinetoffice.eventservice.service;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.shared.dto.DatabaseCredentialsSecret;
import gov.cabinetoffice.shared.exceptions.JsonException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

@RequiredArgsConstructor
@AllArgsConstructor
public class SecretsManagerService {

    @Value("${secrets.database.credentials}")
    private String dbCredsSecretArn;

    private static final Logger logger = LoggerFactory.getLogger(SecretsManagerService.class);

    private final AWSSecretsManager secretsManagerClient;

    private final ObjectMapper objectMapper;

    private DatabaseCredentialsSecret retrievedSecretValue;

    public SecretsManagerService(String dbCredsSecretArn, AWSSecretsManager secretsManagerClient, ObjectMapper objectMapper) {
        this.dbCredsSecretArn = dbCredsSecretArn;
        this.secretsManagerClient = secretsManagerClient;
        this.objectMapper = objectMapper;
    }

    public DatabaseCredentialsSecret getDatabaseCredentialsSecret() {
        if (retrievedSecretValue == null) {
            populateSecretValue();
        }

        return retrievedSecretValue;
    }

    private void populateSecretValue() {
        if (retrievedSecretValue == null) {
            try {
                GetSecretValueRequest valueRequest = new GetSecretValueRequest();
                valueRequest.withSecretId(dbCredsSecretArn);

                String secretString = secretsManagerClient.getSecretValue(valueRequest).getSecretString();

                retrievedSecretValue = objectMapper.readValue(secretString, DatabaseCredentialsSecret.class);
            } catch (SecretsManagerException e) {
                logger.error(e.awsErrorDetails().errorMessage());
                throw e;
            } catch (JsonProcessingException e) {
                throw new JsonException(e.getMessage());
            }
        }
    }

}
