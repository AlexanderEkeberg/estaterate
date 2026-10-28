package org.example.crimerate.service;

import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.dto.CrimeRateResponseDTO;
import org.example.crimerate.entity.CrimeAreaStats;
import org.example.crimerate.events.PropertySubmittedEvent;
import org.example.crimerate.kafka.LatestPropertyState;
import org.example.crimerate.repository.CrimeAreaStatsRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrimeStatsServiceTest {

    @Mock
    private OsloStatbankClient statbankClient;

    @Mock
    private CrimeAreaStatsRepo crimeAreaStatsRepo;

    @Mock
    private LatestPropertyState latestPropertyState;

    @InjectMocks
    private CrimeStatsService crimeStatsService;

    @Test
    void getForProperty_returnsStatsForBydelFromProperty() {
        CrimeAreaStats sagene = new CrimeAreaStats(1L, "3", "Sagene", CrimeAreaStats.AreaType.BYDEL,
                "OSLO_STATBANK_KRI002", 2024, 3906, 47627, 82.0, 59.0);
        when(latestPropertyState.getLatest()).thenReturn(eventWithDistrict("Sagene - Torshov"));
        when(crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, 2024))
                .thenReturn(List.of(sagene));

        assertThat(crimeStatsService.getForProperty(2024))
                .map(CrimeRateResponseDTO::bydel)
                .contains("Sagene");
    }

    @Test
    void getForProperty_isEmptyWhenPropertyHasNoBydel() {
        when(latestPropertyState.getLatest()).thenReturn(eventWithDistrict(null));

        assertThat(crimeStatsService.getForProperty(2024)).isEmpty();
        verifyNoInteractions(crimeAreaStatsRepo, statbankClient);
    }

    @Test
    void getForProperty_isEmptyWhenNoEventIsReceived() {
        when(latestPropertyState.getLatest()).thenReturn(null);

        assertThat(crimeStatsService.getForProperty(2024)).isEmpty();
    }

    @Test
    void buildStats_calculatesRateAndScoreFromRealNumbers() {
        when(statbankClient.getCrimeCounts(2024)).thenReturn(Map.of("1", 9499, "11", 2231));
        when(statbankClient.getPopulation(2024)).thenReturn(Map.of("1", 63721, "11", 34111));

        List<CrimeAreaStats> stats = crimeStatsService.buildStats(2024);

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

        List<CrimeAreaStats> stats = crimeStatsService.buildStats(2024);

        assertThat(stats).extracting(CrimeAreaStats::getAreaName).containsExactly("Gamle Oslo");
    }

    @Test
    void getStats_usesStoredDataWhenPresent() {
        CrimeAreaStats stored = new CrimeAreaStats(1L, "3", "Sagene", CrimeAreaStats.AreaType.BYDEL,
                "OSLO_STATBANK_KRI002", 2024, 3906, 47000, 83.1, 58.4);
        when(crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, 2024))
                .thenReturn(List.of(stored));

        assertThat(crimeStatsService.getStats(2024)).containsExactly(stored);
        verifyNoInteractions(statbankClient);
    }

    @Test
    void getStats_fetchesAndSavesWhenMissing() {
        when(crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, 2024))
                .thenReturn(List.of());
        when(statbankClient.getCrimeCounts(2024)).thenReturn(Map.of("1", 9499, "11", 2231));
        when(statbankClient.getPopulation(2024)).thenReturn(Map.of("1", 63721, "11", 34111));
        when(crimeAreaStatsRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        assertThat(crimeStatsService.getStats(2024))
                .extracting(CrimeAreaStats::getAreaName)
                .containsExactly("Gamle Oslo", "Stovner");
        verify(crimeAreaStatsRepo).saveAll(anyList());
    }

    @Test
    void score_isClampedBetween0And100() {
        assertThat(crimeStatsService.score(0)).isEqualTo(100.0);
        assertThat(crimeStatsService.score(100)).isEqualTo(50.0);
        assertThat(crimeStatsService.score(300)).isEqualTo(0.0);
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

    private PropertySubmittedEvent eventWithDistrict(String district) {
        return new PropertySubmittedEvent(UUID.randomUUID(), "PropertySubmitted", 1, Instant.now(),
                "analysis-1", "https://www.finn.no/realestate/homes/ad.html?finnkode=1", "0474",
                59.93, 10.76, "0301", district);
    }
}
