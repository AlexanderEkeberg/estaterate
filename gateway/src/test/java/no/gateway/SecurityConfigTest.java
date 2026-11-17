package no.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.cloud.consul.enabled=false")
class SecurityConfigTest {

    private static final String ID = "e7ecd150-00af-42fb-9ca6-a215684703f3";

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    void compareRequiresAValidTokenButTheReportStaysOpen() {
        client.get().uri("/api/compare/" + ID).exchange()
                .expectStatus().isUnauthorized();

        client.get().uri("/api/compare/" + ID).header("Authorization", "Bearer not-a-real-token").exchange()
                .expectStatus().isUnauthorized();

        HttpStatus analysis = HttpStatus.resolve(client.get().uri("/api/analysis/" + ID).exchange()
                .returnResult(Void.class).getStatus().value());
        assertThat(analysis).isNotEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
