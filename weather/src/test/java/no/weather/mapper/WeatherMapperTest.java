package no.weather.mapper;

import no.weather.client.dto.OpenMeteoResponse;
import no.weather.dto.MonthlyAverage;
import no.weather.dto.MonthlyWeather;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        private final List<Double> sun = new ArrayList<>();
        private final List<Double> wind = new ArrayList<>();

        Days day(LocalDate date, double mean, double max, double min,
                 double precipitation, double snow, double sunSeconds, double wind) {
            time.add(date);
            this.mean.add(mean);
            this.max.add(max);
            this.min.add(min);
            this.precipitation.add(precipitation);
            this.snow.add(snow);
            this.sun.add(sunSeconds);
            this.wind.add(wind);
            return this;
        }

        // dag der arkivet ikke har data ennå
        Days emptyDay(LocalDate date) {
            time.add(date);
            for (List<Double> values : List.of(mean, max, min, precipitation, snow, sun, wind)) {
                values.add(null);
            }
            return this;
        }

        // alle dagene i måneden med samme verdier
        Days month(YearMonth month, double mean, double max, double min,
                   double precipitation, double snow, double sunSeconds, double wind) {
            for (int d = 1; d <= month.lengthOfMonth(); d++) {
                day(month.atDay(d), mean, max, min, precipitation, snow, sunSeconds, wind);
            }
            return this;
        }

        OpenMeteoResponse build() {
            return new OpenMeteoResponse(new OpenMeteoResponse.Daily(
                    time, mean, max, min, precipitation, snow, sun, wind));
        }
    }

    private MonthlyWeather january(int year, double avgTemp, double precipitation, int frostDays) {
        return new MonthlyWeather(year, 1, avgTemp, avgTemp + 3, avgTemp - 3, precipitation,
                10, 20.0, 40.0, 25.0, frostDays, true);
    }

    @Test
    void aggregatesAFullMonth() {
        OpenMeteoResponse response = new Days()
                .month(YearMonth.of(2025, 1), -4.0, -1.0, -8.0, 2.0, 1.5, 3600, 20.0)
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
            days.day(LocalDate.of(2025, 10, d), 6.0, 9.0, 3.0, 2.0, 0.0, 3600, 10.0);
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
}
