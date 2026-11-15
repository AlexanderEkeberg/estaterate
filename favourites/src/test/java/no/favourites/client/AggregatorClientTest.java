package no.favourites.client;

import no.favourites.exception.AggregatorUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AggregatorClientTest {

    private static final UUID ID = UUID.fromString("e7ecd150-00af-42fb-9ca6-a215684703f3");
    private static final String URL = "http://aggregator/api/analysis/" + ID;

    private static final String CATEGORIES = """
            "categories": {
              "WEATHER": {"status": "SUCCESS", "score": 72, "data": {"forecast": []}, "error": null},
              "SCHOOL": {"status": "PENDING", "score": null, "data": null, "error": null}
            }""";

    private MockRestServiceServer server;
    private AggregatorClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new AggregatorClient(restTemplate, "http://aggregator");
    }

    @Test
    void readsStatusScoresAndAddressFromTheAnalysis() {
        server.expect(requestTo(URL)).andRespond(withSuccess("""
                {"analysisId": "%s", "status": "DONE", "categories": {
                  "WEATHER": {"status": "SUCCESS", "score": 72, "data": {"forecast": []}, "error": null},
                  "SCHOOL": {"status": "PENDING", "score": null, "data": null, "error": null},
                  "PROPERTY": {"status": "SUCCESS", "score": null, "error": null,
                    "data": {"address": "Niels Juels gate 7, 0272 Oslo", "askingPrice": 17800000, "usableArea": 179}}
                }}
                """.formatted(ID), MediaType.APPLICATION_JSON));

        AnalysisSummary summary = client.getAnalysis(ID).orElseThrow();

        assertThat(summary.status()).isEqualTo("DONE");
        assertThat(summary.scores()).containsEntry("WEATHER", 72).containsEntry("SCHOOL", null);
        assertThat(summary.address()).isEqualTo("Niels Juels gate 7, 0272 Oslo");
        assertThat(summary.askingPrice()).isEqualTo(17_800_000L);
        assertThat(summary.usableArea()).isEqualTo(179);
        server.verify();
    }

    @Test
    void worksBeforeAggregatorSendsPropertyDetails() {
        server.expect(requestTo(URL)).andRespond(withSuccess("""
                {"analysisId": "%s", "status": "RUNNING", %s}
                """.formatted(ID, CATEGORIES), MediaType.APPLICATION_JSON));

        AnalysisSummary summary = client.getAnalysis(ID).orElseThrow();

        assertThat(summary.status()).isEqualTo("RUNNING");
        assertThat(summary.address()).isNull();
        assertThat(summary.askingPrice()).isNull();
    }

    @Test
    void unknownAnalysisIsEmptyButFailuresMeanUnavailable() {
        server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThat(client.getAnalysis(ID)).isEmpty();

        server.reset();
        server.expect(requestTo(URL)).andRespond(withServerError());
        assertThatThrownBy(() -> client.getAnalysis(ID)).isInstanceOf(AggregatorUnavailableException.class);

        server.reset();
        server.expect(requestTo(URL)).andRespond(request -> {
            throw new IllegalStateException("No instances available for aggregator");
        });
        assertThatThrownBy(() -> client.getAnalysis(ID)).isInstanceOf(AggregatorUnavailableException.class);
    }
}
