package com.example.aggregator.client;

import com.example.aggregator.dto.PropertyDetailsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpStatus.NOT_FOUND;

class PropertyClientTest {

    private static final String BASE_URL =
            "http://property:8090/api/properties";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private PropertyClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();

        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.build()).thenReturn(restTemplate);

        client = new PropertyClient(builder, BASE_URL);
    }

    @Test
    void returnsPropertyDetails() {
        String propertyUrl = "https://example.com/property/123";

        server.expect(requestTo(
                        BASE_URL + "/details?url=https://example.com/property/123"))
                .andRespond(withSuccess("""
                        {
                          "address": "Niels Juels gate 7, 0272 Oslo",
                          "postalCode": "0272",
                          "askingPrice": 17800000,
                          "usableArea": 179
                        }
                        """, MediaType.APPLICATION_JSON));

        PropertyDetailsDTO details = client.getDetails(propertyUrl).orElseThrow();

        assertThat(details.address()).isEqualTo("Niels Juels gate 7, 0272 Oslo");
        assertThat(details.postalCode()).isEqualTo("0272");
        assertThat(details.askingPrice()).isEqualTo(17_800_000);
        assertThat(details.usableArea()).isEqualTo(179);

        server.verify();
    }

    @Test
    void returnsEmptyWhenPropertyDoesNotExist() {
        String propertyUrl = "https://example.com/property/missing";

        server.expect(requestTo(
                        BASE_URL + "/details?url=https://example.com/property/missing"))
                .andRespond(withStatus(NOT_FOUND));

        assertThat(client.getDetails(propertyUrl)).isEmpty();

        server.verify();
    }
}