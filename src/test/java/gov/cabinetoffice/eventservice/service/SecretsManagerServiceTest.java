package gov.cabinetoffice.eventservice.service;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.cabinetoffice.eventservice.exceptions.JsonException;
import gov.cabinetoffice.shared.dto.DatabaseCredentialsSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SecretsManagerServiceTest {

    private final AWSSecretsManager secretsManagerClient = Mockito.mock(AWSSecretsManager.class);

    private final ObjectMapper mockedObjectMapper = Mockito.mock(ObjectMapper.class);

    private final GetSecretValueResult mockedSecretValueResponse = Mockito.mock(GetSecretValueResult.class);

    private final SecretsManagerException mockedSecretsManagerException = Mockito.mock(SecretsManagerException.class);
    private final AwsErrorDetails mockedAwsErrorDetails = Mockito.mock(AwsErrorDetails.class);
    private final String dbCredsSecretArn = "ARN";

    private SecretsManagerService secretsManagerService;

    @BeforeEach
    void setUp() {
        secretsManagerService = new SecretsManagerService(dbCredsSecretArn, secretsManagerClient, mockedObjectMapper);
    }

    @Nested
    class getDatabaseCredentialsSecret {
        @Test
        void successfulCall() throws JsonProcessingException {

            DatabaseCredentialsSecret secret = DatabaseCredentialsSecret.builder().build();

            when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                    .thenReturn(mockedSecretValueResponse);
            when(mockedSecretValueResponse.getSecretString()).thenReturn("");
            when(mockedObjectMapper.readValue(anyString(), eq(DatabaseCredentialsSecret.class)))
                    .thenReturn(secret);

            secretsManagerService.getDatabaseCredentialsSecret();

            verify(secretsManagerClient).getSecretValue(any(GetSecretValueRequest.class));
        }

        @Test
        void secondCallDoesntHitSecretsManager() throws JsonProcessingException {

            DatabaseCredentialsSecret secret = DatabaseCredentialsSecret.builder().build();

            when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                    .thenReturn(mockedSecretValueResponse);
            when(mockedSecretValueResponse.getSecretString()).thenReturn("");
            when(mockedObjectMapper.readValue(anyString(), eq(DatabaseCredentialsSecret.class)))
                    .thenReturn(secret);

            secretsManagerService.getDatabaseCredentialsSecret();

            verify(secretsManagerClient).getSecretValue(any(GetSecretValueRequest.class));

            secretsManagerService.getDatabaseCredentialsSecret();

            verifyNoMoreInteractions(secretsManagerClient);

        }

        @Test
        void secretsManagerClientThrowsException() {

            DatabaseCredentialsSecret secret = DatabaseCredentialsSecret.builder().build();

            when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                    .thenThrow(mockedSecretsManagerException);
            when(mockedSecretsManagerException.awsErrorDetails()).thenReturn(mockedAwsErrorDetails);
            when(mockedAwsErrorDetails.errorMessage()).thenReturn("Error message");


            assertThrows(SecretsManagerException.class, () -> secretsManagerService.getDatabaseCredentialsSecret());

        }


        @Test
        void jsonProcessingException() throws JsonProcessingException {

            DatabaseCredentialsSecret secret = DatabaseCredentialsSecret.builder().build();

            when(secretsManagerClient.getSecretValue(any(GetSecretValueRequest.class)))
                    .thenReturn(mockedSecretValueResponse);
            when(mockedSecretValueResponse.getSecretString()).thenReturn("");
            when(mockedObjectMapper.readValue(anyString(), eq(DatabaseCredentialsSecret.class)))
                    .thenThrow(JsonProcessingException.class);

            assertThrows(JsonException.class, () -> secretsManagerService.getDatabaseCredentialsSecret());

            verify(secretsManagerClient).getSecretValue(any(GetSecretValueRequest.class));

        }
    }

}