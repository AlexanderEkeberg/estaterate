package org.example.crimerate.service;

import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.client.StatbankException;
import org.example.crimerate.dto.CrimeData;
import org.example.crimerate.dto.CrimeYear;
import org.example.crimerate.dto.DataSource;
import org.example.crimerate.entity.CrimeAreaStats;
import org.example.crimerate.events.PropertySubmittedEvent;
import org.example.crimerate.events.ScoreEvent;
import org.example.crimerate.events.ScoreStatus;
import org.example.crimerate.mock.MockCrimeData;
import org.example.crimerate.repository.CrimeAreaStatsRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CrimeStatsServiceTest {

    private final OsloStatbankClient statbankClient = mock(OsloStatbankClient.class);
    private final CrimeAreaStatsRepo crimeAreaStatsRepo = mock(CrimeAreaStatsRepo.class);
    private final MockCrimeData mockData = new MockCrimeData();

    private static CrimeAreaStats stored(String code, String name, int year, int crimes, int people, double rate, double score) {
        return new CrimeAreaStats(null, code, name, CrimeAreaStats.AreaType.BYDEL,
                "OSLO_STATBANK_KRI002", year, crimes, people, rate, score);
    }

    private static List<CrimeAreaStats> storedFor(int year) {
        return List.of(
                stored("3", "Sagene", year, 4000, 50000, 80.0, 60.0),
                stored("5", "Frogner", year, 2000, 50000, 40.0, 80.0));
    }

    @BeforeEach
    void statsAreStored() {
        for (int year = 2017; year <= 2024; year++) {
            when(crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, year))
                    .thenReturn(storedFor(year));
        }
    }

    private CrimeStatsServiceImpl service(boolean fallbackEnabled, boolean force) {
        CrimeStatsServiceImpl service = new CrimeStatsServiceImpl(statbankClient, crimeAreaStatsRepo, mockData);
        ReflectionTestUtils.setField(service, "fallbackEnabled", fallbackEnabled);
        ReflectionTestUtils.setField(service, "mockForce", force);
        return service;
    }

    private void nothingStored() {
        when(crimeAreaStatsRepo.findByAreaTypeAndYear(eq(CrimeAreaStats.AreaType.BYDEL), anyInt())).thenReturn(List.of());
    }

    private void nothingStoredAndStatbankDown() {
        nothingStored();
        when(statbankClient.getCrimeCounts(anyInt())).thenThrow(new StatbankException("Statbank down"));
    }

    private PropertySubmittedEvent event(String analysisId, String district) {
        return new PropertySubmittedEvent(null, null, 1, null, analysisId, null, null,
                59.93, 10.76, "FINN", "0301", district);
    }

    private CrimeData data(ScoreEvent result) {
        return (CrimeData) result.data();
    }

    @Test
    void storedStatsGiveLiveData() {
        ScoreEvent result = service(true, false).calculate(event("a1", "Sagene - Torshov"));

        assertThat(result.analysisId()).isEqualTo("a1");
        assertThat(result.category()).isEqualTo("CRIME");
        assertThat(result.status()).isEqualTo(ScoreStatus.SUCCESS);
        assertThat(result.score()).isEqualTo(60);
        List<CrimeYear> history = IntStream.rangeClosed(2017, 2024)
                .mapToObj(year -> new CrimeYear(year, 4000, 50000, 80.0, 60.0, 2, 2))
                .toList();
        assertThat(data(result)).isEqualTo(new CrimeData(DataSource.LIVE, "Sagene", 2024, 4000, 50000, 80.0, 60.0, history));
        verifyNoInteractions(statbankClient);
    }

    @Test
    void missingStatsAreFetchedFromStatbankAndSaved() {
        nothingStored();
        when(statbankClient.getCrimeCounts(anyInt())).thenReturn(Map.of("3", 3906));
        when(statbankClient.getPopulation(anyInt())).thenReturn(Map.of("3", 47627));
        when(crimeAreaStatsRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        ScoreEvent result = service(true, false).calculate(event("a1", "Sagene - Torshov"));

        assertThat(result.status()).isEqualTo(ScoreStatus.SUCCESS);
        assertThat(result.score()).isEqualTo(59);
        assertThat(data(result).source()).isEqualTo(DataSource.LIVE);
        assertThat(data(result).ratePer1000()).isEqualTo(82.0);
        assertThat(data(result).history()).hasSize(8);
        verify(crimeAreaStatsRepo, times(8)).saveAll(anyList());
    }

    @Test
    void statbankFailingGivesMockDataWithoutSaving() {
        nothingStoredAndStatbankDown();

        ScoreEvent result = service(true, false).calculate(event("a1", "Sagene - Torshov"));

        assertThat(result.status()).isEqualTo(ScoreStatus.SUCCESS);
        assertThat(result.score()).isEqualTo(59);
        CrimeData data = data(result);
        assertThat(data.source()).isEqualTo(DataSource.MOCK);
        assertThat(data.year()).isEqualTo(2024);
        assertThat(data.crimeCount()).isEqualTo(3906);
        assertThat(data.ratePer1000()).isEqualTo(82.0);
        assertThat(data.history()).extracting(CrimeYear::year)
                .containsExactly(2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024);
        assertThat(data.history().getFirst().crimeCount()).isEqualTo(3871);
        assertThat(data.history()).allMatch(year -> year.districtCount() == 15);
        verify(crimeAreaStatsRepo, never()).saveAll(anyList());
    }

    @Test
    void forceUsesMockWithoutCallingStatbankOrDb() {
        ScoreEvent result = service(true, true).calculate(event("a1", "Sagene - Torshov"));

        assertThat(data(result).source()).isEqualTo(DataSource.MOCK);
        assertThat(data(result).history()).hasSize(8);
        verifyNoInteractions(statbankClient, crimeAreaStatsRepo);
    }

    @Test
    void statbankFailingWithFallbackDisabledGivesFailed() {
        nothingStoredAndStatbankDown();

        ScoreEvent result = service(false, false).calculate(event("a1", "Sagene - Torshov"));

        assertThat(result.status()).isEqualTo(ScoreStatus.FAILED);
        assertThat(result.error()).isEqualTo("Statbank down");
        assertThat(result.data()).isNull();
        assertThat(result.score()).isNull();
    }

    @Test
    void unexpectedExceptionGivesFailedInsteadOfThrowing() {
        when(crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, 2024))
                .thenThrow(new IllegalStateException("oops"));

        ScoreEvent result = service(true, false).calculate(event("a1", "Sagene - Torshov"));

        assertThat(result.status()).isEqualTo(ScoreStatus.FAILED);
        assertThat(result.error()).isEqualTo("oops");
    }

    @Test
    void missingDistrictGivesFailedWithoutLookingUpStats() {
        ScoreEvent result = service(true, false).calculate(event("a1", null));

        assertThat(result.status()).isEqualTo(ScoreStatus.FAILED);
        assertThat(result.error()).isEqualTo("Mangler bydel");
        verifyNoInteractions(statbankClient, crimeAreaStatsRepo);
    }

    @Test
    void districtWithoutBydelGivesFailed() {
        ScoreEvent result = service(true, false).calculate(event("a1", "Sentrum"));

        assertThat(result.status()).isEqualTo(ScoreStatus.FAILED);
        assertThat(result.error()).isEqualTo("Ukjent bydel: Sentrum");
        verifyNoInteractions(statbankClient, crimeAreaStatsRepo);
    }

    @Test
    void bydelWithoutStatsGivesFailed() {
        ScoreEvent result = service(true, false).calculate(event("a1", "Gamle Oslo"));

        assertThat(result.status()).isEqualTo(ScoreStatus.FAILED);
        assertThat(result.error()).isEqualTo("Mangler tall for Gamle Oslo");
    }

    @Test
    void eachEventKeepsItsOwnAnalysisId() {
        CrimeStatsServiceImpl service = service(true, false);

        ScoreEvent first = service.calculate(event("first", "Sagene - Torshov"));
        ScoreEvent second = service.calculate(event("second", "Sagene - Torshov"));

        assertThat(first.analysisId()).isEqualTo("first");
        assertThat(second.analysisId()).isEqualTo("second");
    }

    @Test
    void buildStats_calculatesRateAndScoreFromRealNumbers() {
        when(statbankClient.getCrimeCounts(2024)).thenReturn(Map.of("1", 9499, "11", 2231));
        when(statbankClient.getPopulation(2024)).thenReturn(Map.of("1", 63721, "11", 34111));

        List<CrimeAreaStats> stats = service(true, false).buildStats(2024);

        assertThat(stats).hasSize(2);
        CrimeAreaStats gamleOslo = stats.getFirst();
        assertThat(gamleOslo.getAreaName()).isEqualTo("Gamle Oslo");
        assertThat(gamleOslo.getRatePer1000()).isCloseTo(149.1, within(0.1));
        assertThat(gamleOslo.getScore()).isCloseTo(25.5, within(0.1));

        CrimeAreaStats stovner = stats.get(1);
        assertThat(stovner.getAreaName()).isEqualTo("Stovner");
        assertThat(stovner.getRatePer1000()).isCloseTo(65.4, within(0.1));
        assertThat(stovner.getScore()).isCloseTo(67.3, within(0.1));
    }

    @Test
    void buildStats_skipsBydelWithMissingPopulation() {
        when(statbankClient.getCrimeCounts(2024)).thenReturn(Map.of("1", 9499, "3", 3906));
        when(statbankClient.getPopulation(2024)).thenReturn(Map.of("1", 63721));

        List<CrimeAreaStats> stats = service(true, false).buildStats(2024);

        assertThat(stats).extracting(CrimeAreaStats::getAreaName).containsExactly("Gamle Oslo");
    }

    @Test
    void getStats_usesStoredDataWhenPresent() {
        assertThat(service(true, false).getStats(2024)).containsExactlyElementsOf(storedFor(2024));
        verifyNoInteractions(statbankClient);
    }

    @Test
    void history_osloAverageIsTotalCrimesOverTotalPopulation() {
        Map<Integer, List<CrimeAreaStats>> statsByYear = new TreeMap<>(Map.of(2024, List.of(
                stored("3", "Sagene", 2024, 4000, 50000, 80.0, 60.0),
                stored("5", "Frogner", 2024, 1000, 5000, 200.0, 0.0))));

        List<CrimeYear> history = service(true, false).toHistory(statsByYear, "3");

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().osloRatePer1000()).isEqualTo(90.9);
    }

    @Test
    void history_rankIsOneForTheLowestRate() {
        Map<Integer, List<CrimeAreaStats>> statsByYear = new TreeMap<>(Map.of(2024, List.of(
                stored("3", "Sagene", 2024, 4000, 50000, 80.0, 60.0),
                stored("5", "Frogner", 2024, 2000, 50000, 40.0, 80.0),
                stored("1", "Gamle Oslo", 2024, 9000, 60000, 150.0, 25.0))));

        CrimeStatsServiceImpl service = service(true, false);

        assertThat(service.toHistory(statsByYear, "5").getFirst().rank()).isEqualTo(1);
        assertThat(service.toHistory(statsByYear, "3").getFirst().rank()).isEqualTo(2);
        assertThat(service.toHistory(statsByYear, "1").getFirst().rank()).isEqualTo(3);
        assertThat(service.toHistory(statsByYear, "1").getFirst().districtCount()).isEqualTo(3);
    }

    @Test
    void history_skipsYearsWithoutStatsForTheBydel() {
        Map<Integer, List<CrimeAreaStats>> statsByYear = new TreeMap<>(Map.of(
                2023, List.of(stored("5", "Frogner", 2023, 2000, 50000, 40.0, 80.0)),
                2024, storedFor(2024)));

        List<CrimeYear> history = service(true, false).toHistory(statsByYear, "3");

        assertThat(history).extracting(CrimeYear::year).containsExactly(2024);
    }

    @Test
    void score_isClampedBetween0And100() {
        CrimeStatsServiceImpl service = service(true, false);

        assertThat(service.score(0)).isEqualTo(100.0);
        assertThat(service.score(100)).isEqualTo(50.0);
        assertThat(service.score(300)).isEqualTo(0.0);
    }

    @ParameterizedTest
    @CsvSource({
            "Gamle Oslo, GAMLE_OSLO",
            "Helsfyr - Sinsen, GAMLE_OSLO",
            "Grünerløkka - Sofienberg, GRUNERLOKKA",
            "Sagene - Torshov, SAGENE",
            "St.Hanshaugen - Ullevål, ST_HANSHAUGEN",
            "Bygdøy - Frogner, FROGNER",
            "Uranienborg - Majorstuen, FROGNER",
            "Ullern, ULLERN",
            "Vinderen, VESTRE_AKER",
            "Røa, VESTRE_AKER",
            "Grefsen - Kjelsås, NORDRE_AKER",
            "Sogn, NORDRE_AKER",
            "Bjerke, BJERKE",
            "Grorud, GRORUD",
            "Romsås, GRORUD",
            "Stovner, STOVNER",
            "Furuset, ALNA",
            "Hellerud, ALNA",
            "Østensjø, OSTENSJO",
            "Bøler, OSTENSJO",
            "Manglerud, OSTENSJO",
            "Nordstrand, NORDSTRAND",
            "Lambertseter, NORDSTRAND",
            "Ekeberg - Bekkelaget, NORDSTRAND",
            "Søndre Nordstrand, SONDRE_NORDSTRAND"
    })
    void osloBydel_fromFinnDistrict_mapsEveryFinnArea(String finnArea, OsloBydel expected) {
        assertThat(OsloBydel.fromFinnDistrict(finnArea)).contains(expected);
    }

    @Test
    void osloBydel_fromFinnDistrict_isEmptyForAreasWithoutBydel() {
        assertThat(OsloBydel.fromFinnDistrict("Sentrum")).isEmpty();
        assertThat(OsloBydel.fromFinnDistrict("Marka")).isEmpty();
        assertThat(OsloBydel.fromFinnDistrict(null)).isEmpty();
    }
}
