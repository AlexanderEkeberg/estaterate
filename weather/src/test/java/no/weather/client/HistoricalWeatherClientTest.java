package no.weather.client;


import no.weather.client.dto.OpenMeteoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestTemplateBuilder;

public class HistoricalWeatherClientTest {
    @Test
    public void testHistoricalWeatherClient() {
        HistoricalWeatherClient client = new HistoricalWeatherClient(new RestTemplateBuilder());
        OpenMeteoResponse history = client.getHistory(59.9252, 10.7264);
        System.out.println(history.daily().time().size() + " days, first: " + history.daily().time().get(0)
                + ", last: " + history.daily().time().get(history.daily().time().size() - 1));
    }
}
