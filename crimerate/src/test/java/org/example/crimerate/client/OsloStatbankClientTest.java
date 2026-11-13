package org.example.crimerate.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OsloStatbankClientTest {

    private static final String CRIMES = """
            {"data": [
              {"key": ["1", "0", "7"], "values": ["9499"]},
              {"key": ["3", "0", "7"], "values": ["3906"]},
              {"key": ["4", "0", "7"], "values": [".."]}
            ]}
            """;

    private static final String POPULATION = """
            {"data": [
              {"key": ["7", "1", "0"], "values": ["63721"]}
            ]}
            """;

    private MockRestServiceServer server;
    private OsloStatbankClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new OsloStatbankClient(restTemplate, "http://test/db1");
    }

    @Test
    void readsCrimeCountsPerBydelAndSkipsMissingValues() {
        server.expect(once(), requestTo("http://test/db1/Kriminalitet/KRI002.px"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(CRIMES, MediaType.APPLICATION_JSON));

        assertThat(client.getCrimeCounts(2024)).containsExactlyInAnyOrderEntriesOf(
                Map.of("1", 9499, "3", 3906));
        server.verify();
    }

    @Test
    void readsPopulationWithBydelAsSecondKey() {
        server.expect(once(), requestTo("http://test/db1/Befolkning/Folkemengde/OK-BEF005.px"))
                .andRespond(withSuccess(POPULATION, MediaType.APPLICATION_JSON));

        assertThat(client.getPopulation(2024)).containsEntry("1", 63721);
    }

    @Test
    void throwsAfterRetryOnServerError() {
        server.expect(times(2), anything()).andRespond(withServerError());

        assertThatThrownBy(() -> client.getCrimeCounts(2024)).isInstanceOf(StatbankException.class);
        server.verify();
    }

    @Test
    void throwsWhenThereIsNoData() {
        server.expect(once(), anything()).andRespond(withSuccess("{\"data\": []}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getCrimeCounts(2024)).isInstanceOf(StatbankException.class);
    }
}
