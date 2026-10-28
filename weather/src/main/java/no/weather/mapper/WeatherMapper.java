package no.weather.mapper;

import no.weather.client.dto.OpenMeteoResponse;
import no.weather.client.dto.YrResponse;
import no.weather.dto.DailyForecast;
import no.weather.dto.MonthlyAverage;
import no.weather.dto.MonthlyWeather;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
    private static final ZoneId OSLO = ZoneId.of("Europe/Oslo");
    private static final int FORECAST_DAYS = 10;

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

    // Yr har ett tidspunkt per time de første dagene (med next_1_hours) og deretter hver sjette time
    // (bare next_6_hours). Vi bruker bare én av periodene per tidspunkt, så ingenting telles to ganger.
    public List<DailyForecast> toForecast(YrResponse response) {
        Map<LocalDate, List<YrResponse.TimeSeries>> stepsPerDay = response.properties().timeseries().stream()
                .filter(step -> step.time() != null && step.data() != null)
                .collect(Collectors.groupingBy(step -> step.time().atZone(OSLO).toLocalDate(),
                        TreeMap::new, Collectors.toList()));

        return stepsPerDay.entrySet().stream()
                .map(entry -> toDay(entry.getKey(), entry.getValue()))
                .flatMap(Optional::stream)
                .limit(FORECAST_DAYS)
                .toList();
    }

    private Optional<DailyForecast> toDay(LocalDate date, List<YrResponse.TimeSeries> steps) {
        List<Double> temperatures = steps.stream()
                .flatMap(step -> temperaturesOf(step.data()).stream())
                .toList();
        if (temperatures.isEmpty()) {
            return Optional.empty();
        }

        double precipitation = steps.stream()
                .map(step -> precipitationOf(step.data()))
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        return Optional.of(new DailyForecast(
                date,
                round(temperatures.stream().mapToDouble(Double::doubleValue).min().getAsDouble()),
                round(temperatures.stream().mapToDouble(Double::doubleValue).max().getAsDouble()),
                round(precipitation),
                symbolNearMidday(steps)));
    }

    // timesdelen bruker temperaturen på tidspunktet, 6-timersdelen også min og maks for perioden
    private List<Double> temperaturesOf(YrResponse.Data data) {
        List<Double> temperatures = new ArrayList<>();
        if (data.instant() != null && data.instant().details() != null) {
            temperatures.add(data.instant().details().airTemperature());
        }
        YrResponse.PeriodDetails sixHours = detailsOf(data.next6Hours());
        if (!isHourly(data) && sixHours != null) {
            temperatures.add(sixHours.airTemperatureMax());
            temperatures.add(sixHours.airTemperatureMin());
        }
        temperatures.removeIf(Objects::isNull);
        return temperatures;
    }

    private Double precipitationOf(YrResponse.Data data) {
        YrResponse.PeriodDetails details = detailsOf(isHourly(data) ? data.next1Hours() : data.next6Hours());
        return details == null ? null : details.precipitationAmount();
    }

    private String symbolNearMidday(List<YrResponse.TimeSeries> steps) {
        return steps.stream()
                .filter(step -> symbolOf(step.data()) != null)
                .min(Comparator.comparingLong(step -> Math.abs(
                        Duration.between(LocalTime.NOON, step.time().atZone(OSLO).toLocalTime()).toMinutes())))
                .map(step -> symbolOf(step.data()))
                .orElse(null);
    }

    private String symbolOf(YrResponse.Data data) {
        return Stream.of(symbolOf(data.next6Hours()), symbolOf(data.next1Hours()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String symbolOf(YrResponse.Period period) {
        return period == null || period.summary() == null ? null : period.summary().symbolCode();
    }

    private YrResponse.PeriodDetails detailsOf(YrResponse.Period period) {
        return period == null ? null : period.details();
    }

    private boolean isHourly(YrResponse.Data data) {
        return data.next1Hours() != null;
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
