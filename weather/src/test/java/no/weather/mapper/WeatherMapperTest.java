package no.weather.mapper;

import no.weather.client.dto.OpenMeteoResponse;
import no.weather.client.dto.YrResponse;
import no.weather.dto.DailyForecast;
import no.weather.dto.MonthlyAverage;
import no.weather.dto.MonthlyWeather;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeatherMapperTest {

    private final WeatherMapper mapper = new WeatherMapper();

    // bygger et Open-Meteo-svar dag for dag
    private static class Days {
        private final List<LocalDate> time = new ArrayList<>();
        private final List<Double> mean = new ArrayList<>();
        private final List<Double> max = new ArrayList<>();
        private final List<Double> min = new ArrayList<>();
        private final List<Double> precipitation = new ArrayList<>();
        private final List<Double> snow = new ArrayList<>();
        private final List<Double> wind = new ArrayList<>();
        private final List<LocalDateTime> hourTime = new ArrayList<>();
        private final List<Double> directNormal = new ArrayList<>();
        private final List<Double> cloudCover = new ArrayList<>();

        Days day(LocalDate date, double mean, double max, double min,
                 double precipitation, double snow, int sunnyHours, double wind) {
            time.add(date);
            this.mean.add(mean);
            this.max.add(max);
            this.min.add(min);
            this.precipitation.add(precipitation);
            this.snow.add(snow);
            this.wind.add(wind);
            for (int h = 0; h < 24; h++) {
                boolean sunny = h < sunnyHours;
                hours(date.atTime(h, 0), sunny ? 500.0 : 0.0, sunny ? 10.0 : 100.0);
            }
            return this;
        }

        // dag der arkivet ikke har data ennå
        Days emptyDay(LocalDate date) {
            time.add(date);
            for (List<Double> values : List.of(mean, max, min, precipitation, snow, wind)) {
                values.add(null);
            }
            for (int h = 0; h < 24; h++) {
                hours(date.atTime(h, 0), null, null);
            }
            return this;
        }

        // alle dagene i måneden med samme verdier
        Days month(YearMonth month, double mean, double max, double min,
                   double precipitation, double snow, int sunnyHours, double wind) {
            for (int d = 1; d <= month.lengthOfMonth(); d++) {
                day(month.atDay(d), mean, max, min, precipitation, snow, sunnyHours, wind);
            }
            return this;
        }

        Days setHour(LocalDateTime hour, Double directNormal, Double cloudCover) {
            int i = hourTime.indexOf(hour);
            this.directNormal.set(i, directNormal);
            this.cloudCover.set(i, cloudCover);
            return this;
        }

        private void hours(LocalDateTime hour, Double directNormal, Double cloudCover) {
            hourTime.add(hour);
            this.directNormal.add(directNormal);
            this.cloudCover.add(cloudCover);
        }

        OpenMeteoResponse build() {
            return new OpenMeteoResponse(
                    new OpenMeteoResponse.Daily(time, mean, max, min, precipitation, snow, wind),
                    new OpenMeteoResponse.Hourly(hourTime, directNormal, cloudCover));
        }
    }

    private MonthlyWeather january(int year, double avgTemp, double precipitation, int frostDays) {
        return new MonthlyWeather(year, 1, avgTemp, avgTemp + 3, avgTemp - 3, precipitation,
                10, 20.0, 40.0, 25.0, frostDays, true);
    }

    @Test
    void aggregatesAFullMonth() {
        OpenMeteoResponse response = new Days()
                .month(YearMonth.of(2025, 1), -4.0, -1.0, -8.0, 2.0, 1.5, 1, 20.0)
                .build();

        List<MonthlyWeather> monthly = mapper.toMonthly(response);

        assertEquals(1, monthly.size());
        MonthlyWeather january = monthly.get(0);
        assertEquals(2025, january.year());
        assertEquals(1, january.month());
        assertEquals(-4.0, january.avgTempC());
        assertEquals(-1.0, january.avgMaxTempC());
        assertEquals(-8.0, january.avgMinTempC());
        assertEquals(62.0, january.precipitationMm());       // 31 dager * 2 mm
        assertEquals(46.5, january.snowfallCm());            // 31 dager * 1,5 cm
        assertEquals(31.0, january.sunshineHours());         // 31 dager * 1 time
        assertEquals(20.0, january.avgMaxWindKmh());
        assertEquals(31, january.rainyDays());
        assertEquals(31, january.frostDays());
        assertTrue(january.complete());
    }

    @Test
    void countsRainyAndFrostDaysByThreshold() {
        Days days = new Days();
        // 10 tørre dager, 10 dager med akkurat 1 mm nedbør og -0,1 i minimum, 11 dager med 0,9 mm
        for (int d = 1; d <= 10; d++) {
            days.day(LocalDate.of(2025, 3, d), 3.0, 5.0, 0.0, 0.0, 0.0, 0, 10.0);
        }
        for (int d = 11; d <= 20; d++) {
            days.day(LocalDate.of(2025, 3, d), -1.0, 1.0, -0.1, 1.0, 0.0, 0, 10.0);
        }
        for (int d = 21; d <= 31; d++) {
            days.day(LocalDate.of(2025, 3, d), 2.0, 4.0, 0.0, 0.9, 0.0, 0, 10.0);
        }

        MonthlyWeather march = mapper.toMonthly(days.build()).get(0);

        assertEquals(10, march.rainyDays());   // 1,0 mm teller, 0,9 mm teller ikke
        assertEquals(10, march.frostDays());   // -0,1 teller, 0,0 teller ikke
    }

    @Test
    void groupsPerYearAndMonthInOrder() {
        OpenMeteoResponse response = new Days()
                .month(YearMonth.of(2024, 1), -3.0, 0.0, -6.0, 1.0, 0.0, 0, 10.0)
                .month(YearMonth.of(2024, 2), -2.0, 1.0, -5.0, 1.0, 0.0, 0, 10.0)
                .month(YearMonth.of(2025, 1), -5.0, -1.0, -9.0, 1.0, 0.0, 0, 10.0)
                .build();

        List<MonthlyWeather> monthly = mapper.toMonthly(response);

        assertEquals(3, monthly.size());
        assertEquals(List.of(2024, 2024, 2025), monthly.stream().map(MonthlyWeather::year).toList());
        assertEquals(List.of(1, 2, 1), monthly.stream().map(MonthlyWeather::month).toList());
    }

    @Test
    void skipsDaysWithoutDataAndFlagsMonthAsIncomplete() {
        Days days = new Days();
        for (int d = 1; d <= 15; d++) {
            days.day(LocalDate.of(2025, 10, d), 6.0, 9.0, 3.0, 2.0, 0.0, 1, 10.0);
        }
        for (int d = 16; d <= 31; d++) {
            days.emptyDay(LocalDate.of(2025, 10, d));
        }

        List<MonthlyWeather> monthly = mapper.toMonthly(days.build());

        assertEquals(1, monthly.size());
        MonthlyWeather october = monthly.get(0);
        assertFalse(october.complete());
        assertEquals(6.0, october.avgTempC());
        assertEquals(30.0, october.precipitationMm());   // bare de 15 dagene med data
    }

    @Test
    void skipsMonthsWithTooFewDays() {
        Days days = new Days();
        for (int d = 1; d <= 3; d++) {
            days.day(LocalDate.of(2025, 11, d), 1.0, 3.0, -1.0, 0.0, 0.0, 0, 5.0);
        }
        for (int d = 4; d <= 30; d++) {
            days.emptyDay(LocalDate.of(2025, 11, d));
        }

        assertTrue(mapper.toMonthly(days.build()).isEmpty());
    }

    @Test
    void countsOnlyHoursWithStrongDirectSunAndFewClouds() {
        LocalDate first = LocalDate.of(2025, 6, 1);
        OpenMeteoResponse response = new Days()
                .month(YearMonth.of(2025, 6), 15.0, 20.0, 10.0, 0.0, 0.0, 0, 10.0)
                .setHour(first.atTime(10, 0), 121.0, 69.0)
                .setHour(first.atTime(11, 0), 120.0, 0.0)
                .setHour(first.atTime(12, 0), 800.0, 70.0)
                .setHour(first.atTime(13, 0), 400.0, 20.0)
                .build();

        assertEquals(2.0, mapper.toMonthly(response).get(0).sunshineHours());
    }

    @Test
    void skipsDaysWithMissingHourlyValues() {
        OpenMeteoResponse response = new Days()
                .month(YearMonth.of(2025, 6), 15.0, 20.0, 10.0, 0.0, 0.0, 5, 10.0)
                .setHour(LocalDate.of(2025, 6, 30).atTime(23, 0), null, null)
                .build();

        MonthlyWeather june = mapper.toMonthly(response).get(0);

        assertFalse(june.complete());
        assertEquals(145.0, june.sunshineHours());
    }

    @Test
    void averagesEachCalendarMonthOverTheYears() {
        List<MonthlyWeather> monthly = List.of(
                january(2023, -4.0, 40.0, 30),
                january(2024, -2.0, 60.0, 20),
                january(2025, -3.0, 50.0, 25));

        List<MonthlyAverage> averages = mapper.toMonthlyAverages(monthly);

        assertEquals(1, averages.size());
        MonthlyAverage january = averages.get(0);
        assertEquals(1, january.month());
        assertEquals(-3.0, january.avgTempC());
        assertEquals(0.0, january.avgMaxTempC());
        assertEquals(-6.0, january.avgMinTempC());
        assertEquals(50.0, january.precipitationMm());   // snitt av månedssummene
        assertEquals(25.0, january.frostDays());
        assertEquals(10.0, january.rainyDays());
    }

    @Test
    void averagesIgnoreIncompleteMonths() {
        MonthlyWeather incomplete = new MonthlyWeather(2025, 1, 20.0, 25.0, 15.0, 999.0,
                1, 0.0, 0.0, 0.0, 0, false);
        List<MonthlyWeather> monthly = List.of(
                january(2023, -4.0, 40.0, 30),
                january(2024, -2.0, 60.0, 20),
                incomplete);

        MonthlyAverage january = mapper.toMonthlyAverages(monthly).get(0);

        assertEquals(-3.0, january.avgTempC());
        assertEquals(50.0, january.precipitationMm());
    }

    @Test
    void omitsMonthsWithTooFewCompleteYears() {
        MonthlyWeather februaryOnlyOnce = new MonthlyWeather(2025, 2, -2.0, 1.0, -5.0, 30.0,
                5, 10.0, 60.0, 20.0, 20, true);
        List<MonthlyWeather> monthly = List.of(
                january(2024, -2.0, 60.0, 20),
                january(2025, -4.0, 40.0, 30),
                februaryOnlyOnce);

        List<MonthlyAverage> averages = mapper.toMonthlyAverages(monthly);

        assertEquals(List.of(1), averages.stream().map(MonthlyAverage::month).toList());
    }

    @Test
    void averagesAreSortedByMonth() {
        MonthlyWeather feb2024 = new MonthlyWeather(2024, 2, -2.0, 1.0, -5.0, 30.0,
                5, 10.0, 60.0, 20.0, 20, true);
        MonthlyWeather feb2025 = new MonthlyWeather(2025, 2, -3.0, 0.0, -6.0, 40.0,
                6, 12.0, 70.0, 22.0, 22, true);
        List<MonthlyWeather> monthly = List.of(
                feb2024, feb2025, january(2024, -2.0, 60.0, 20), january(2025, -4.0, 40.0, 30));

        List<MonthlyAverage> averages = mapper.toMonthlyAverages(monthly);

        assertEquals(List.of(1, 2), averages.stream().map(MonthlyAverage::month).toList());
    }

    // --- Yr-varsel ---

    // tidspunkt med timesverdi (next_1_hours), som de første dagene hos Yr
    private static YrResponse.TimeSeries hour(String utcTime, double temp, double precipitation, String symbol) {
        return new YrResponse.TimeSeries(Instant.parse(utcTime), new YrResponse.Data(
                new YrResponse.InstantData(new YrResponse.InstantDetails(temp)),
                new YrResponse.Period(new YrResponse.Summary(symbol),
                        new YrResponse.PeriodDetails(precipitation, null, null)),
                null));
    }

    // tidspunkt med 6-timersverdi (next_6_hours), som resten av varselet
    private static YrResponse.TimeSeries sixHours(String utcTime, double temp, double precipitation,
                                                  double max, double min, String symbol) {
        return new YrResponse.TimeSeries(Instant.parse(utcTime), new YrResponse.Data(
                new YrResponse.InstantData(new YrResponse.InstantDetails(temp)),
                null,
                new YrResponse.Period(new YrResponse.Summary(symbol),
                        new YrResponse.PeriodDetails(precipitation, max, min))));
    }

    private static YrResponse forecastOf(YrResponse.TimeSeries... steps) {
        return new YrResponse(new YrResponse.Properties(List.of(steps)));
    }

    @Test
    void forecastForAnHourlyDay() {
        // 06, 10 og 14 UTC er 08, 12 og 16 i Oslo
        YrResponse response = forecastOf(
                hour("2026-10-22T06:00:00Z", 5.0, 0.2, "cloudy"),
                hour("2026-10-22T10:00:00Z", 8.0, 0.5, "rain"),
                hour("2026-10-22T14:00:00Z", 6.0, 0.3, "clearsky_day"));

        List<DailyForecast> forecast = mapper.toForecast(response);

        assertEquals(1, forecast.size());
        DailyForecast day = forecast.get(0);
        assertEquals(LocalDate.of(2026, 10, 22), day.date());
        assertEquals(5.0, day.minTempC());
        assertEquals(8.0, day.maxTempC());
        assertEquals(1.0, day.precipitationMm());
        assertEquals("rain", day.symbol());
    }

    @Test
    void forecastForASixHourlyDayUsesMinAndMaxOfEachPeriod() {
        // 00, 06, 12 og 18 UTC er 02, 08, 14 og 20 i Oslo
        YrResponse response = forecastOf(
                sixHours("2026-10-26T00:00:00Z", 4.0, 0.0, 5.0, 2.0, "clearsky_night"),
                sixHours("2026-10-26T06:00:00Z", 5.0, 1.0, 8.0, 3.0, "cloudy"),
                sixHours("2026-10-26T12:00:00Z", 9.0, 2.5, 11.0, 7.0, "rain"),
                sixHours("2026-10-26T18:00:00Z", 6.0, 0.5, 8.0, 4.0, "partlycloudy_night"));

        DailyForecast day = mapper.toForecast(response).get(0);

        assertEquals(2.0, day.minTempC());
        assertEquals(11.0, day.maxTempC());
        assertEquals(4.0, day.precipitationMm());
        assertEquals("rain", day.symbol());   // 14:00 i Oslo er nærmest kl. 12
    }

    @Test
    void groupsByDateInOsloAndNotUtc() {
        // 23:30 og 00:30 i Oslo, men samme dato i UTC
        YrResponse response = forecastOf(
                hour("2026-10-22T21:30:00Z", 5.0, 0.0, "cloudy"),
                hour("2026-10-22T22:30:00Z", 4.0, 0.0, "cloudy"));

        List<DailyForecast> forecast = mapper.toForecast(response);

        assertEquals(List.of(LocalDate.of(2026, 10, 22), LocalDate.of(2026, 10, 23)),
                forecast.stream().map(DailyForecast::date).toList());
    }

    @Test
    void hourlyStepsDoNotAlsoCountTheirSixHourPeriod() {
        // timestidspunkt med både next_1_hours og next_6_hours, slik Yr sender dem
        YrResponse.TimeSeries step = new YrResponse.TimeSeries(Instant.parse("2026-10-22T10:00:00Z"),
                new YrResponse.Data(
                        new YrResponse.InstantData(new YrResponse.InstantDetails(8.0)),
                        new YrResponse.Period(new YrResponse.Summary("rain"),
                                new YrResponse.PeriodDetails(0.5, null, null)),
                        new YrResponse.Period(new YrResponse.Summary("cloudy"),
                                new YrResponse.PeriodDetails(9.9, 30.0, -30.0))));

        DailyForecast day = mapper.toForecast(forecastOf(step)).get(0);

        assertEquals(0.5, day.precipitationMm());
        assertEquals(8.0, day.minTempC());
        assertEquals(8.0, day.maxTempC());
    }

    @Test
    void skipsDaysWithoutTemperatureAndToleratesMissingFields() {
        YrResponse.TimeSeries withoutTemperature = new YrResponse.TimeSeries(
                Instant.parse("2026-10-22T10:00:00Z"), new YrResponse.Data(null, null, null));
        YrResponse.TimeSeries withoutPeriods = new YrResponse.TimeSeries(
                Instant.parse("2026-10-23T10:00:00Z"), new YrResponse.Data(
                new YrResponse.InstantData(new YrResponse.InstantDetails(7.0)), null, null));

        List<DailyForecast> forecast = mapper.toForecast(forecastOf(withoutTemperature, withoutPeriods));

        assertEquals(1, forecast.size());
        assertEquals(LocalDate.of(2026, 10, 23), forecast.get(0).date());
        assertEquals(0.0, forecast.get(0).precipitationMm());
        assertNull(forecast.get(0).symbol());
    }

    @Test
    void forecastIsSortedAndLimitedToTenDays() {
        YrResponse.TimeSeries[] steps = new YrResponse.TimeSeries[12];
        for (int i = 0; i < 12; i++) {
            steps[i] = hour(LocalDate.of(2026, 10, 12).plusDays(11 - i) + "T10:00:00Z", 5.0, 0.0, "cloudy");
        }

        List<DailyForecast> forecast = mapper.toForecast(forecastOf(steps));

        assertEquals(10, forecast.size());
        assertEquals(LocalDate.of(2026, 10, 12), forecast.get(0).date());
        assertEquals(LocalDate.of(2026, 10, 21), forecast.get(9).date());
    }
}
