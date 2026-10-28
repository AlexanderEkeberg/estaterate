package no.weather.client;

import no.weather.client.dto.YrResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class YrClientTest {

    private static final String FORECAST = """
            {"properties": {"timeseries": [
              {"time": "2026-10-22T10:00:00Z",
               "data": {
                 "instant": {"details": {"air_temperature": 7.4}},
                 "next_1_hours": {"summary": {"symbol_code": "lightrain"},
                                  "details": {"precipitation_amount": 0.6}},
                 "next_6_hours": {"summary": {"symbol_code": "cloudy"},
                                  "details": {"precipitation_amount": 1.2,
                                              "air_temperature_max": 8.1,
                                              "air_temperature_min": 5.9}}
               }}
            ]}}
            """;

    private MockRestServiceServer server;
    private YrClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new YrClient(restTemplate, "http://test/forecast");
    }

    @Test
    void parsesForecast() {
        server.expect(once(), anything()).andRespond(withSuccess(FORECAST, MediaType.APPLICATION_JSON));
        YrResponse forecast = client.getForecast(59.9252, 10.7264);

        YrResponse.TimeSeries first = forecast.properties().timeseries().get(0);
        assertEquals(7.4, first.data().instant().details().airTemperature());
        assertEquals("lightrain", first.data().next1Hours().summary().symbolCode());
        assertEquals(8.1, first.data().next6Hours().details().airTemperatureMax());
    }

    @Test
    void sendsCoordinatesAndUserAgent() {
        server.expect(once(), anything())
                .andExpect(queryParam("lat", "59.9252"))
                .andExpect(queryParam("lon", "10.7264"))
                .andExpect(header("User-Agent", startsWith("estaterate-weather")))
                .andRespond(withSuccess(FORECAST, MediaType.APPLICATION_JSON));
        client.getForecast(59.9252, 10.7264);

        server.verify();
    }

    @Test
    void throwsAfterRetryOnServerError() {
        server.expect(times(2), anything()).andRespond(withServerError());
        assertThrows(WeatherApiException.class, () -> client.getForecast(59.9, 10.7));

        server.verify();
    }

    @Test
    void throwsWhenThereIsNoTimeseries() {
        server.expect(once(), anything())
                .andRespond(withSuccess("{\"properties\": {\"timeseries\": []}}", MediaType.APPLICATION_JSON));

        assertThrows(WeatherApiException.class, () -> client.getForecast(59.9, 10.7));
    }
}
