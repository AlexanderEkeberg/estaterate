package no.weather.client;

import no.weather.client.dto.OpenMeteoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class HistoricalWeatherClientTest {

    private static final String TWO_DAYS = """
            {"daily": {
              "time": ["2025-01-01", "2025-01-02"],
              "temperature_2m_mean": [-5.3, -11.4],
              "temperature_2m_max": [-4.1, -9.1],
              "temperature_2m_min": [-9.5, -13.7],
              "precipitation_sum": [3.7, 0.0],
              "snowfall_sum": [2.45, 0.0],
              "wind_speed_10m_max": [21.1, 13.2]
            },
            "hourly": {
              "time": ["2025-01-01T12:00", "2025-01-02T12:00"],
              "direct_normal_irradiance": [0.0, 310.5],
              "cloud_cover": [100.0, 12.0]
            }}
            """;

    private MockRestServiceServer server;
    private HistoricalWeatherClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new HistoricalWeatherClient(restTemplate, "http://test/archive");
    }

    @Test
    void parsesDailyValues() {
        server.expect(once(), anything()).andRespond(withSuccess(TWO_DAYS, MediaType.APPLICATION_JSON));

        OpenMeteoResponse history = client.getHistory(59.9252, 10.7264);

        assertEquals(2, history.daily().time().size());
        assertEquals(LocalDate.of(2025, 1, 2), history.daily().time().get(1));
        assertEquals(-5.3, history.daily().temperatureMean().get(0));
        assertEquals(13.2, history.daily().windSpeedMax().get(1));
    }

    @Test
    void parsesHourlyValues() {
        server.expect(once(), anything()).andRespond(withSuccess(TWO_DAYS, MediaType.APPLICATION_JSON));

        OpenMeteoResponse.Hourly hourly = client.getHistory(59.9252, 10.7264).hourly();

        assertEquals(LocalDateTime.of(2025, 1, 2, 12, 0), hourly.time().get(1));
        assertEquals(310.5, hourly.directNormalIrradiance().get(1));
        assertEquals(12.0, hourly.cloudCover().get(1));
    }

    @Test
    void requestsFiveFullCalendarYearsWithAllVariables() {
        int startYear = LocalDate.now().minusDays(5).getYear() - 5;
        server.expect(requestTo(containsString("http://test/archive")))
                .andExpect(queryParam("latitude", "59.9252"))
                .andExpect(queryParam("longitude", "10.7264"))
                .andExpect(queryParam("start_date", startYear + "-01-01"))
                .andExpect(queryParam("daily", containsString("temperature_2m_mean")))
                .andExpect(queryParam("daily", containsString("snowfall_sum")))
                .andExpect(queryParam("daily", containsString("wind_speed_10m_max")))
                .andExpect(queryParam("hourly", "direct_normal_irradiance,cloud_cover"))
                .andExpect(queryParam("timezone", "Europe/Oslo"))
                .andRespond(withSuccess(TWO_DAYS, MediaType.APPLICATION_JSON));

        client.getHistory(59.9252, 10.7264);

        server.verify();
    }

    @Test
    void throwsAfterRetryOnServerError() {
        server.expect(times(2), anything()).andRespond(withServerError());

        assertThrows(WeatherApiException.class, () -> client.getHistory(59.9, 10.7));

        server.verify();
    }

    @Test
    void doesNotRetryOnClientError() {
        server.expect(once(), anything()).andRespond(withBadRequest());

        assertThrows(WeatherApiException.class, () -> client.getHistory(59.9, 10.7));

        server.verify();
    }

    @Test
    void throwsWhenDailyIsMissing() {
        server.expect(once(), anything()).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThrows(WeatherApiException.class, () -> client.getHistory(59.9, 10.7));
    }

    @Test
    void throwsWhenThereAreNoDays() {
        String empty = """
                {"daily": {"time": [], "temperature_2m_mean": [], "temperature_2m_max": [],
                "temperature_2m_min": [], "precipitation_sum": [], "snowfall_sum": [],
                "wind_speed_10m_max": []}}
                """;
        server.expect(once(), anything()).andRespond(withSuccess(empty, MediaType.APPLICATION_JSON));

        assertThrows(WeatherApiException.class, () -> client.getHistory(59.9, 10.7));
    }

    @Test
    void throwsWhenListsHaveDifferentLength() {
        String mismatched = """
                {"daily": {"time": ["2025-01-01", "2025-01-02"],
                "temperature_2m_mean": [-5.3], "temperature_2m_max": [-4.1, -9.1],
                "temperature_2m_min": [-9.5, -13.7], "precipitation_sum": [3.7, 0.0],
                "snowfall_sum": [2.45, 0.0], "wind_speed_10m_max": [21.1, 13.2]},
                "hourly": {"time": ["2025-01-01T12:00"], "direct_normal_irradiance": [0.0],
                "cloud_cover": [100.0]}}
                """;
        server.expect(once(), anything()).andRespond(withSuccess(mismatched, MediaType.APPLICATION_JSON));

        assertThrows(WeatherApiException.class, () -> client.getHistory(59.9, 10.7));
    }

    @Test
    void throwsWhenHourlyIsMissing() {
        String withoutHourly = """
                {"daily": {"time": ["2025-01-01"], "temperature_2m_mean": [-5.3],
                "temperature_2m_max": [-4.1], "temperature_2m_min": [-9.5], "precipitation_sum": [3.7],
                "snowfall_sum": [2.45], "wind_speed_10m_max": [21.1]}}
                """;
        server.expect(once(), anything()).andRespond(withSuccess(withoutHourly, MediaType.APPLICATION_JSON));

        assertThrows(WeatherApiException.class, () -> client.getHistory(59.9, 10.7));
    }

    @Test
    void throwsWhenHourlyListsHaveDifferentLength() {
        String mismatched = TWO_DAYS.replace("\"cloud_cover\": [100.0, 12.0]", "\"cloud_cover\": [100.0]");
        server.expect(once(), anything()).andRespond(withSuccess(mismatched, MediaType.APPLICATION_JSON));

        assertThrows(WeatherApiException.class, () -> client.getHistory(59.9, 10.7));
    }
}
