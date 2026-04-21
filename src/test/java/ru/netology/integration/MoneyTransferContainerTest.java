package ru.netology.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Testcontainers(disabledWithoutDocker = true)
class MoneyTransferContainerTest {

    @BeforeAll
    static void checkDocker() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker is not available, skipping integration tests");
    }

    @Container
    private static final GenericContainer<?> appContainer =
            new GenericContainer<>(DockerImageName.parse("money-transfer-service:latest"))
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/transfer").forStatusCode(405));

    @Test
    void testTransferAndConfirm() {
        String baseUrl = "http://" + appContainer.getHost() + ":" + appContainer.getMappedPort(8080);
        RestTemplate rest = new RestTemplate();

        // 3000 копеек = 30 рублей
        String transferJson = """
                {
                    "cardFromNumber": "1111222233334444",
                    "cardFromValidTill": "12/25",
                    "cardFromCVV": "123",
                    "cardToNumber": "5555666677778888",
                    "amount": { "value": 3000, "currency": "RUB" }
                }
                """;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> transferResp = rest.postForEntity(
                baseUrl + "/transfer", new HttpEntity<>(transferJson, headers), String.class);
        assertEquals(200, transferResp.getStatusCodeValue());
        String opId = extractOperationId(transferResp.getBody());
        assertNotNull(opId);

        String confirmJson = String.format("{\"operationId\":\"%s\",\"code\":\"0000\"}", opId);
        ResponseEntity<String> confirmResp = rest.postForEntity(
                baseUrl + "/confirmOperation", new HttpEntity<>(confirmJson, headers), String.class);
        assertEquals(200, confirmResp.getStatusCodeValue());
        assertTrue(confirmResp.getBody().contains(opId));
    }

    private String extractOperationId(String json) {
        int start = json.indexOf("\"operationId\":\"") + 15;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}