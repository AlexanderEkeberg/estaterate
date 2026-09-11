package no.weather.client;


import org.junit.jupiter.api.Test;
import org.springframework.boot.restclient.RestTemplateBuilder;

public class YrClientTest {
    @Test
    public void testYrClient() {
        YrClient yrClient = new YrClient(new RestTemplateBuilder());
        String forecast = yrClient.getForecast(59.9252, 10.7264);
        System.out.println(forecast);
    }
}
