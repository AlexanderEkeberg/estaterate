package org.example.crimerate.service;

import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.entity.CrimeAreaStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrimeStatsServiceTest {

    @Mock
    private OsloStatbankClient statbankClient;

    @InjectMocks
    private CrimeStatsService crimeStatsService;

    @Test
    void buildStats_calculatesRateAndScoreFromRealNumbers() {
        // Ekte tall for 2024
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
    void score_isClampedBetween0And100() {
        assertThat(crimeStatsService.score(0)).isEqualTo(100.0);
        assertThat(crimeStatsService.score(100)).isEqualTo(50.0);
        assertThat(crimeStatsService.score(300)).isEqualTo(0.0);
    }

    @Test
    void osloBydel_fromName_ignoresCaseAndSpaces() {
        assertThat(OsloBydel.fromName("Sagene")).contains(OsloBydel.SAGENE);
        assertThat(OsloBydel.fromName("sagene")).contains(OsloBydel.SAGENE);
        assertThat(OsloBydel.fromName("St.Hanshaugen")).contains(OsloBydel.ST_HANSHAUGEN);
        assertThat(OsloBydel.fromName("Sentrum")).isEmpty();
    }
}
