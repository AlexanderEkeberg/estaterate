package org.example.crimerate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.client.StatbankException;
import org.example.crimerate.dto.CrimeData;
import org.example.crimerate.dto.CrimeYear;
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
import java.util.TreeMap;

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
            log.atWarn()
                    .addKeyValue("event_type", "district_missing")
                    .addKeyValue("analysis_id", analysisId)
                    .log("Property has no district");
            return ScoreEvent.failed(analysisId, CATEGORY, "Mangler bydel");
        }
        Optional<OsloBydel> bydel = OsloBydel.fromFinnDistrict(event.district());
        if (bydel.isEmpty()) {
            log.atWarn()
                    .addKeyValue("event_type", "district_unknown")
                    .addKeyValue("analysis_id", analysisId)
                    .addKeyValue("district", event.district())
                    .log("Unknown district");
            return ScoreEvent.failed(analysisId, CATEGORY, "Ukjent bydel: " + event.district());
        }

        try {
            DataSource source = DataSource.LIVE;
            Map<Integer, List<CrimeAreaStats>> statsByYear = liveHistory(analysisId);
            if (statsByYear == null) {
                source = DataSource.MOCK;
                statsByYear = mockHistory();
            }

            String code = bydel.get().getCode();
            DataSource usedSource = source;
            List<CrimeYear> history = toHistory(statsByYear, code);
            return findArea(statsByYear.getOrDefault(OsloStatbankClient.LAST_YEAR, List.of()), code)
                    .map(s -> ScoreEvent.success(analysisId, CATEGORY, (int) Math.round(s.getScore()),
                            toData(s, usedSource, history)))
                    .orElseGet(() -> ScoreEvent.failed(analysisId, CATEGORY,
                            "Mangler tall for " + bydel.get().getDisplayName()));
        } catch (Exception e) {
            log.atError()
                    .setCause(e)
                    .addKeyValue("event_type", "score_calculation_failed")
                    .addKeyValue("analysis_id", analysisId)
                    .log("Crime score calculation failed");
            return ScoreEvent.failed(analysisId, CATEGORY, e.getMessage());
        }
    }

    private Map<Integer, List<CrimeAreaStats>> liveHistory(String analysisId) {
        if (mockForce) {
            return null;
        }
        Map<Integer, List<CrimeAreaStats>> statsByYear = new TreeMap<>();
        for (int year = OsloStatbankClient.FIRST_YEAR; year <= OsloStatbankClient.LAST_YEAR; year++) {
            try {
                statsByYear.put(year, getStats(year));
            } catch (StatbankException e) {
                if (!fallbackEnabled) {
                    throw e;
                }
                log.atWarn()
                        .addKeyValue("event_type", "mock_fallback_used")
                        .addKeyValue("analysis_id", analysisId)
                        .addKeyValue("year", year)
                        .addKeyValue("reason", e.getMessage())
                        .log("Using mock crime stats");
                return null;
            }
        }
        return statsByYear;
    }

    private Map<Integer, List<CrimeAreaStats>> mockHistory() {
        Map<Integer, List<CrimeAreaStats>> statsByYear = new TreeMap<>();
        for (int year : mock.years()) {
            statsByYear.put(year, toStats(year, mock.crimeCounts(year), mock.population(year)));
        }
        return statsByYear;
    }

    List<CrimeYear> toHistory(Map<Integer, List<CrimeAreaStats>> statsByYear, String areaCode) {
        List<CrimeYear> history = new ArrayList<>();
        statsByYear.forEach((year, stats) ->
                findArea(stats, areaCode).ifPresent(own -> history.add(toYear(own, stats))));
        return history;
    }

    private CrimeYear toYear(CrimeAreaStats own, List<CrimeAreaStats> stats) {
        int crimes = stats.stream().mapToInt(CrimeAreaStats::getCrimeCount).sum();
        int people = stats.stream().mapToInt(CrimeAreaStats::getPopulation).sum();
        int rank = 1 + (int) stats.stream()
                .filter(s -> s.getRatePer1000() < own.getRatePer1000())
                .count();
        return new CrimeYear(
                own.getYear(),
                own.getCrimeCount(),
                own.getPopulation(),
                roundOneDecimal(own.getRatePer1000()),
                roundOneDecimal(ratePer1000(crimes, people)),
                rank,
                stats.size()
        );
    }

    private Optional<CrimeAreaStats> findArea(List<CrimeAreaStats> stats, String areaCode) {
        return stats.stream().filter(s -> s.getAreaCode().equals(areaCode)).findFirst();
    }

    List<CrimeAreaStats> getStats(int year) {
        List<CrimeAreaStats> stored = crimeAreaStatsRepo.findByAreaTypeAndYear(CrimeAreaStats.AreaType.BYDEL, year);
        if (!stored.isEmpty()) {
            return sortByBydelCode(stored);
        }
        log.atInfo()
                .addKeyValue("event_type", "stats_fetch_started")
                .addKeyValue("year", year)
                .log("No stored crime stats, fetching from statbank");
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
                log.atWarn()
                        .addKeyValue("event_type", "stats_missing")
                        .addKeyValue("district", bydel.getDisplayName())
                        .addKeyValue("year", year)
                        .log("Missing crime stats for district");
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

    private CrimeData toData(CrimeAreaStats stats, DataSource source, List<CrimeYear> history) {
        return new CrimeData(
                source,
                stats.getAreaName(),
                stats.getYear(),
                stats.getCrimeCount(),
                stats.getPopulation(),
                roundOneDecimal(stats.getRatePer1000()),
                roundOneDecimal(stats.getScore()),
                history
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
