package no.weather.service;

import no.weather.client.HistoricalWeatherClient;
import no.weather.client.WeatherApiException;
import no.weather.client.YrClient;
import no.weather.client.dto.OpenMeteoResponse;
import no.weather.client.dto.YrResponse;
import no.weather.dto.DailyForecast;
import no.weather.dto.DataSource;
import no.weather.dto.WeatherData;
import no.weather.events.PropertySubmittedEvent;
import no.weather.events.ScoreEvent;
import no.weather.events.ScoreStatus;
import no.weather.mapper.WeatherMapper;
import no.weather.mock.MockWeatherData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WeatherServiceTest {

    private final HistoricalWeatherClient historicalClient = mock(HistoricalWeatherClient.class);
    private final YrClient yrClient = mock(YrClient.class);
    private final WeatherMapper mapper = mock(WeatherMapper.class);
    private final MockWeatherData mock = new MockWeatherData();

    // "live"-data fra mapperen er bevisst mindre enn mock-dataen, så vi ser hvilken som er brukt
    private final DailyForecast liveDay = new DailyForecast(LocalDate.now(), 1, 5, 0, "clearsky_day");

    @BeforeEach
    void apisWork() {
        when(historicalClient.getHistory(anyDouble(), anyDouble())).thenReturn(mock(OpenMeteoResponse.class));
        when(yrClient.getForecast(anyDouble(), anyDouble())).thenReturn(mock(YrResponse.class));
        when(mapper.toMonthly(org.mockito.ArgumentMatchers.any())).thenReturn(mock.monthly().subList(0, 3));
        when(mapper.toMonthlyAverages(org.mockito.ArgumentMatchers.any())).thenReturn(mock.monthlyAverages().subList(0, 1));
        when(mapper.toForecast(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(liveDay));
    }

    private WeatherServiceImpl service(boolean fallbackEnabled, String force) {
        WeatherServiceImpl service = new WeatherServiceImpl(yrClient, historicalClient, mapper, mock);
        ReflectionTestUtils.setField(service, "fallbackEnabled", fallbackEnabled);
        ReflectionTestUtils.setField(service, "mockForce", force);
        return service;
    }

    private PropertySubmittedEvent event(String analysisId, Double lat, Double lon) {
        return new PropertySubmittedEvent(null, null, 1, null, analysisId, null, null, lat, lon, "GEOCODED", null, null);
    }

    private WeatherData data(ScoreEvent result) {
        return (WeatherData) result.data();
    }

    @Test
    void bothApisWorkGivesLiveData() {
        ScoreEvent result = service(true, "").calculate(event("a1", 59.9, 10.7));

        assertEquals("a1", result.analysisId());
        assertEquals("WEATHER", result.category());
        assertEquals(ScoreStatus.SUCCESS, result.status());
        assertNull(result.score());
        assertEquals(DataSource.LIVE, data(result).sources().history());
        assertEquals(DataSource.LIVE, data(result).sources().forecast());
        assertEquals(1, data(result).monthlyAverages().size());
        assertEquals(List.of(liveDay), data(result).forecast());
        assertNotNull(data(result).forecastFetchedAt());
        assertEquals("GEOCODED", data(result).locationSource());
    }

    @Test
    void yrFailingGivesMockForecastAndLiveHistory() {
        when(yrClient.getForecast(anyDouble(), anyDouble())).thenThrow(new WeatherApiException("Yr down"));

        ScoreEvent result = service(true, "").calculate(event("a1", 59.9, 10.7));

        assertEquals(ScoreStatus.SUCCESS, result.status());
        assertEquals(DataSource.LIVE, data(result).sources().history());
        assertEquals(DataSource.MOCK, data(result).sources().forecast());
        assertEquals(10, data(result).forecast().size());
        assertNull(data(result).forecastFetchedAt());
    }

    @Test
    void openMeteoFailingGivesMockHistoryAndLiveForecast() {
        when(historicalClient.getHistory(anyDouble(), anyDouble())).thenThrow(new WeatherApiException("Open-Meteo down"));

        ScoreEvent result = service(true, "").calculate(event("a1", 59.9, 10.7));

        assertEquals(ScoreStatus.SUCCESS, result.status());
        assertEquals(DataSource.MOCK, data(result).sources().history());
        assertEquals(DataSource.LIVE, data(result).sources().forecast());
        assertEquals(12, data(result).monthlyAverages().size());
        assertEquals(mock.monthly(), data(result).monthly());
    }

    @Test
    void bothApisFailingGivesAllMockData() {
        when(historicalClient.getHistory(anyDouble(), anyDouble())).thenThrow(new WeatherApiException("down"));
        when(yrClient.getForecast(anyDouble(), anyDouble())).thenThrow(new WeatherApiException("down"));

        ScoreEvent result = service(true, "").calculate(event("a1", 59.9, 10.7));

        assertEquals(ScoreStatus.SUCCESS, result.status());
        assertEquals(DataSource.MOCK, data(result).sources().history());
        assertEquals(DataSource.MOCK, data(result).sources().forecast());
    }

    @Test
    void historyWithTooLittleDataGivesMockHistory() {
        when(mapper.toMonthlyAverages(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        ScoreEvent result = service(true, "").calculate(event("a1", 59.9, 10.7));

        assertEquals(DataSource.MOCK, data(result).sources().history());
        assertEquals(12, data(result).monthlyAverages().size());
    }

    @Test
    void forceForecastUsesMockWithoutCallingYr() {
        ScoreEvent result = service(true, "forecast").calculate(event("a1", 59.9, 10.7));

        verify(yrClient, never()).getForecast(anyDouble(), anyDouble());
        assertEquals(DataSource.LIVE, data(result).sources().history());
        assertEquals(DataSource.MOCK, data(result).sources().forecast());
    }

    @Test
    void forceHistoryUsesMockWithoutCallingOpenMeteo() {
        ScoreEvent result = service(true, "history").calculate(event("a1", 59.9, 10.7));

        verify(historicalClient, never()).getHistory(anyDouble(), anyDouble());
        assertEquals(DataSource.MOCK, data(result).sources().history());
        assertEquals(DataSource.LIVE, data(result).sources().forecast());
    }

    @Test
    void apiFailingWithFallbackDisabledGivesFailed() {
        when(yrClient.getForecast(anyDouble(), anyDouble())).thenThrow(new WeatherApiException("Yr down"));

        ScoreEvent result = service(false, "").calculate(event("a1", 59.9, 10.7));

        assertEquals(ScoreStatus.FAILED, result.status());
        assertEquals("Yr down", result.error());
        assertNull(result.data());
    }

    @Test
    void unexpectedExceptionGivesFailedInsteadOfThrowing() {
        when(historicalClient.getHistory(anyDouble(), anyDouble())).thenThrow(new IllegalStateException("oops"));

        ScoreEvent result = service(true, "").calculate(event("a1", 59.9, 10.7));

        assertEquals(ScoreStatus.FAILED, result.status());
        assertEquals("oops", result.error());
    }

    @Test
    void missingCoordinatesGivesFailedWithoutCallingApis() {
        ScoreEvent result = service(true, "").calculate(event("a1", null, null));

        assertEquals(ScoreStatus.FAILED, result.status());
        assertEquals("Mangler koordinater", result.error());
        verifyNoInteractions(historicalClient, yrClient);
    }

    @Test
    void eachEventKeepsItsOwnAnalysisId() {
        WeatherServiceImpl service = service(true, "");

        ScoreEvent first = service.calculate(event("first", 59.9, 10.7));
        ScoreEvent second = service.calculate(event("second", 59.9, 10.7));

        assertEquals("first", first.analysisId());
        assertEquals("second", second.analysisId());
    }
}
