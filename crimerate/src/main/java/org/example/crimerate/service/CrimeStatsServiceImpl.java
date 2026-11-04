package org.example.crimerate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.client.StatbankException;
import org.example.crimerate.dto.CrimeData;
import org.example.crimerate.dto.DataSource;
import org.example.crimerate.entity.CrimeAreaStats;
import org.example.crimerate.events.PropertySubmittedEvent;
import org.example.crimerate.events.ScoreEvent;
import org.example.crimerate.mock.MockCrimeData;
import org.example.crimerate.repository.CrimeAreaStatsRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrimeStatsServiceImpl implements CrimeStatsService {

    private static final String SOURCE = "OSLO_STATBANK_KRI002";
    private static final String CATEGORY = "CRIME";

    private static final double RATE_AT_ZERO_SCORE = 200.0;

    private final OsloStatbankClient statbankClient;
    private final CrimeAreaStatsRepo crimeAreaStatsRepo;
    private final MockCrimeData mock;

    @Value("${crimerate.mock.fallback-enabled}")
    private boolean fallbackEnabled;

    @Value("${crimerate.mock.force:false}")
    private boolean mockForce;

    @Override
    public ScoreEvent calculate(PropertySubmittedEvent event) {
        String analysisId = event.analysisId();
        if (event.district() == null) {
            log.warn("Property has no district: {}", analysisId);
            return ScoreEvent.failed(analysisId, CATEGORY, "Mangler bydel");
        }
        Optional<OsloBydel> bydel = OsloBydel.fromFinnDistrict(event.district());
        if (bydel.isEmpty()) {
            log.warn("Unknown bydel '{}' for {}", event.district(), analysisId);
            return ScoreEvent.failed(analysisId, CATEGORY, "Ukjent bydel: " + event.district());
        }

        try {
            DataSource source = DataSource.LIVE;
            List<CrimeAreaStats> stats = liveStats(OsloStatbankClient.LAST_YEAR);
            if (stats == null) {
                source = DataSource.MOCK;
                stats = toStats(mock.year(), mock.crimeCounts(), mock.population());
            }

            DataSource usedSource = source;
            return stats.stream()
                    .filter(s -> s.getAreaCode().equals(bydel.get().getCode()))
                    .findFirst()
                    .map(s -> ScoreEvent.success(analysisId, CATEGORY, (int) Math.round(s.getScore()), toData(s, usedSource)))
                    .orElseGet(() -> ScoreEvent.failed(analysisId, CATEGORY,
                            "Mangler tall for " + bydel.get().getDisplayName()));
        } catch (Exception e) {
            log.error("Crimerate failed for {}", analysisId, e);
            return ScoreEvent.failed(analysisId, CATEGORY, e.getMessage());
        }
    }

    private List<CrimeAreaStats> liveStats(int year) {
        if (mockForce) {
            return null;
        }
        try {
            return getStats(year);
        } catch (StatbankException e) {
            if (!fallbackEnabled) {
                throw e;
            }
            log.warn("Using mock crime stats: {}", e.getMessage());
            return null;
        }
    }

    List<CrimeAreaStats> getStats(int year) {
        List<CrimeAreaStats> stored = crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, year);
        if (!stored.isEmpty()) {
            return sortByBydelCode(stored);
        }
        log.info("Ingen lagrede tall for {}, henter fra statistikkbanken", year);
        return sortByBydelCode(crimeAreaStatsRepo.saveAll(buildStats(year)));
    }

    private List<CrimeAreaStats> sortByBydelCode(List<CrimeAreaStats> stats) {
        return stats.stream()
                .sorted(Comparator.comparingInt(s -> Integer.parseInt(s.getAreaCode())))
                .toList();
    }

    List<CrimeAreaStats> buildStats(int year) {
        return toStats(year, statbankClient.getCrimeCounts(year), statbankClient.getPopulation(year));
    }

    private List<CrimeAreaStats> toStats(int year, Map<String, Integer> crimeCounts, Map<String, Integer> population) {
        List<CrimeAreaStats> result = new ArrayList<>();
        for (OsloBydel bydel : OsloBydel.values()) {
            Integer crimes = crimeCounts.get(bydel.getCode());
            Integer people = population.get(bydel.getCode());
            if (crimes == null || people == null || people == 0) {
                log.warn("Mangler tall for {} i {}", bydel.getDisplayName(), year);
                continue;
            }

            double rate = ratePer1000(crimes, people);
            result.add(new CrimeAreaStats(
                    null,
                    bydel.getCode(),
                    bydel.getDisplayName(),
                    CrimeAreaStats.AreaType.BYDEL,
                    SOURCE,
                    year,
                    crimes,
                    people,
                    rate,
                    score(rate)
            ));
        }
        return result;
    }

    private CrimeData toData(CrimeAreaStats stats, DataSource source) {
        return new CrimeData(
                source,
                stats.getAreaName(),
                stats.getYear(),
                stats.getCrimeCount(),
                stats.getPopulation(),
                roundOneDecimal(stats.getRatePer1000()),
                roundOneDecimal(stats.getScore())
        );
    }

    private double roundOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    double ratePer1000(int crimes, int population) {
        return crimes * 1000.0 / population;
    }

    double score(double ratePer1000) {
        double score = 100.0 * (1.0 - ratePer1000 / RATE_AT_ZERO_SCORE);
        return Math.max(0.0, Math.min(100.0, score));
    }
}
