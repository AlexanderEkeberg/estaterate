package no.weather.mapper;

import no.weather.client.dto.OpenMeteoResponse;
import no.weather.dto.MonthlyAverage;
import no.weather.dto.MonthlyWeather;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class WeatherMapper {

    // måneder med færre gyldige dager enn dette tas ikke med
    private static final int MIN_DAYS_IN_MONTH = 7;
    // en måned må ha minst så mange komplette år for å få et snitt
    private static final int MIN_YEARS_FOR_AVERAGE = 2;
    private static final double RAINY_DAY_MM = 1.0;

    // én dag fra Open-Meteo. Verdiene er null for dager arkivet ikke har data for ennå
    private record Day(LocalDate date, Double mean, Double max, Double min,
                       Double precipitation, Double snow, Double sunSeconds, Double wind) {

        boolean hasValues() {
            return Stream.of(mean, max, min, precipitation, snow, sunSeconds, wind).noneMatch(Objects::isNull);
        }
    }

    public List<MonthlyWeather> toMonthly(OpenMeteoResponse response) {
        Map<YearMonth, List<Day>> daysPerMonth = readDays(response.daily()).stream()
                .collect(Collectors.groupingBy(day -> YearMonth.from(day.date()), TreeMap::new, Collectors.toList()));

        return daysPerMonth.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MIN_DAYS_IN_MONTH)
                .map(entry -> toMonth(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<MonthlyAverage> toMonthlyAverages(List<MonthlyWeather> monthly) {
        Map<Integer, List<MonthlyWeather>> completeMonths = monthly.stream()
                .filter(MonthlyWeather::complete)
                .collect(Collectors.groupingBy(MonthlyWeather::month, TreeMap::new, Collectors.toList()));

        return completeMonths.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MIN_YEARS_FOR_AVERAGE)
                .map(entry -> toAverage(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<Day> readDays(OpenMeteoResponse.Daily daily) {
        return IntStream.range(0, daily.time().size())
                .mapToObj(i -> new Day(
                        daily.time().get(i),
                        daily.temperatureMean().get(i),
                        daily.temperatureMax().get(i),
                        daily.temperatureMin().get(i),
                        daily.precipitationSum().get(i),
                        daily.snowfallSum().get(i),
                        daily.sunshineDuration().get(i),
                        daily.windSpeedMax().get(i)))
                .filter(Day::hasValues)
                .toList();
    }

    private MonthlyWeather toMonth(YearMonth month, List<Day> days) {
        return new MonthlyWeather(
                month.getYear(),
                month.getMonthValue(),
                average(days, Day::mean),
                average(days, Day::max),
                average(days, Day::min),
                sum(days, Day::precipitation),
                count(days, day -> day.precipitation() >= RAINY_DAY_MM),
                sum(days, Day::snow),
                sum(days, day -> day.sunSeconds() / 3600),
                average(days, Day::wind),
                count(days, day -> day.min() < 0),
                days.size() == month.lengthOfMonth());
    }

    private MonthlyAverage toAverage(int month, List<MonthlyWeather> years) {
        return new MonthlyAverage(
                month,
                average(years, MonthlyWeather::avgTempC),
                average(years, MonthlyWeather::avgMaxTempC),
                average(years, MonthlyWeather::avgMinTempC),
                average(years, MonthlyWeather::precipitationMm),
                average(years, MonthlyWeather::rainyDays),
                average(years, MonthlyWeather::snowfallCm),
                average(years, MonthlyWeather::sunshineHours),
                average(years, MonthlyWeather::avgMaxWindKmh),
                average(years, MonthlyWeather::frostDays));
    }

    private <T> double average(List<T> items, ToDoubleFunction<T> value) {
        return round(items.stream().mapToDouble(value).average().orElse(0));
    }

    private <T> double sum(List<T> items, ToDoubleFunction<T> value) {
        return round(items.stream().mapToDouble(value).sum());
    }

    private <T> int count(List<T> items, Predicate<T> condition) {
        return (int) items.stream().filter(condition).count();
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
