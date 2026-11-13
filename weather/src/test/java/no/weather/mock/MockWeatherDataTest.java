package no.weather.mock;

import no.weather.dto.DailyForecast;
import no.weather.mapper.WeatherMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MockWeatherDataTest {

    private final MockWeatherData mock = new MockWeatherData();

    @Test
    void hasMonthlyValuesForSeveralYears() {
        assertFalse(mock.monthly().isEmpty());
        assertEquals(2021, mock.monthly().get(0).year());
    }

    @Test
    void hasAnAverageForEveryCalendarMonth() {
        List<Integer> months = mock.monthlyAverages().stream().map(average -> average.month()).toList();

        assertEquals(IntStream.rangeClosed(1, 12).boxed().toList(), months);
    }

    @Test
    void averagesMatchTheMonthlyValues() {
        assertEquals(new WeatherMapper().toMonthlyAverages(mock.monthly()), mock.monthlyAverages());
    }

    @Test
    void forecastStartsTodayAndCoversTenConsecutiveDays() {
        List<DailyForecast> forecast = mock.forecast();

        assertEquals(10, forecast.size());
        LocalDate today = LocalDate.now(ZoneId.of("Europe/Oslo"));
        for (int i = 0; i < forecast.size(); i++) {
            assertEquals(today.plusDays(i), forecast.get(i).date());
        }
    }
}
