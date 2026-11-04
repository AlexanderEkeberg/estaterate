package org.example.crimerate.service;

import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.client.StatbankException;
import org.example.crimerate.dto.CrimeData;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CrimeStatsServiceTest {

    private final OsloStatbankClient statbankClient = mock(OsloStatbankClient.class);
    private final CrimeAreaStatsRepo crimeAreaStatsRepo = mock(CrimeAreaStatsRepo.class);
    private final MockCrimeData mockData = new MockCrimeData();

    private final CrimeAreaStats storedSagene = new CrimeAreaStats(1L, "3", "Sagene", CrimeAreaStats.AreaType.BYDEL,
            "OSLO_STATBANK_KRI002", 2024, 4000, 50000, 80.0, 60.0);

    @BeforeEach
    void statsAreStored() {
        when(crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, 2024))
                .thenReturn(List.of(storedSagene));
    }

    private CrimeStatsServiceImpl service(boolean fallbackEnabled, boolean force) {
        CrimeStatsServiceImpl service = new CrimeStatsServiceImpl(statbankClient, crimeAreaStatsRepo, mockData);
        ReflectionTestUtils.setField(service, "fallbackEnabled", fallbackEnabled);
        ReflectionTestUtils.setField(service, "mockForce", force);
        return service;
    }

    private void nothingStoredAndStatbankDown() {
        when(crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, 2024)).thenReturn(List.of());
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
        assertThat(data(result)).isEqualTo(new CrimeData(DataSource.LIVE, "Sagene", 2024, 4000, 50000, 80.0, 60.0));
        verifyNoInteractions(statbankClient);
    }

    @Test
    void missingStatsAreFetchedFromStatbankAndSaved() {
        when(crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, 2024)).thenReturn(List.of());
        when(statbankClient.getCrimeCounts(2024)).thenReturn(Map.of("3", 3906));
        when(statbankClient.getPopulation(2024)).thenReturn(Map.of("3", 47627));
        when(crimeAreaStatsRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        ScoreEvent result = service(true, false).calculate(event("a1", "Sagene - Torshov"));

        assertThat(result.status()).isEqualTo(ScoreStatus.SUCCESS);
        assertThat(result.score()).isEqualTo(59);
        assertThat(data(result).source()).isEqualTo(DataSource.LIVE);
        assertThat(data(result).ratePer1000()).isEqualTo(82.0);
        verify(crimeAreaStatsRepo).saveAll(anyList());
    }

    @Test
    void statbankFailingGivesMockDataWithoutSaving() {
        nothingStoredAndStatbankDown();

        ScoreEvent result = service(true, false).calculate(event("a1", "Sagene - Torshov"));

        assertThat(result.status()).isEqualTo(ScoreStatus.SUCCESS);
        assertThat(result.score()).isEqualTo(59);
        assertThat(data(result)).isEqualTo(new CrimeData(DataSource.MOCK, "Sagene", 2024, 3906, 47627, 82.0, 59.0));
        verify(crimeAreaStatsRepo, never()).saveAll(anyList());
    }

    @Test
    void forceUsesMockWithoutCallingStatbankOrDb() {
        ScoreEvent result = service(true, true).calculate(event("a1", "Sagene - Torshov"));

        assertThat(data(result).source()).isEqualTo(DataSource.MOCK);
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
        assertThat(service(true, false).getStats(2024)).containsExactly(storedSagene);
        verifyNoInteractions(statbankClient);
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
