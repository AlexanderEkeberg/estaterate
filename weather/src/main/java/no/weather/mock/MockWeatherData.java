package no.weather.mock;

import no.weather.dto.DailyForecast;
import no.weather.dto.MonthlyAverage;
import no.weather.dto.MonthlyWeather;
import no.weather.dto.WeatherData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

// eksempeldata (Oslo) som brukes når et api ikke svarer. Ligger i mock-weather.json
@Component
public class MockWeatherData {

    private static final String FILE = "mock-weather.json";
    private static final ZoneId OSLO = ZoneId.of("Europe/Oslo");

    private final WeatherData data;

    public MockWeatherData() {
        try (InputStream file = new ClassPathResource(FILE).getInputStream()) {
            this.data = JsonMapper.builder().build().readValue(file, WeatherData.class);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + FILE, e);
        }
    }

    public List<MonthlyWeather> monthly() {
        return data.monthly();
    }

    public List<MonthlyAverage> monthlyAverages() {
        return data.monthlyAverages();
    }

    // datoene flyttes slik at varselet starter i dag
    public List<DailyForecast> forecast() {
        long shiftDays = ChronoUnit.DAYS.between(data.forecast().get(0).date(), LocalDate.now(OSLO));
        return data.forecast().stream()
                .map(day -> new DailyForecast(day.date().plusDays(shiftDays), day.minTempC(),
                        day.maxTempC(), day.precipitationMm(), day.symbol()))
                .toList();
    }
}
