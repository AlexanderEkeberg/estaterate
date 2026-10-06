package org.example.crimerate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.crimerate.client.OsloStatbankClient;
import org.example.crimerate.client.PropertyClient;
import org.example.crimerate.dto.CrimeRateResponseDTO;
import org.example.crimerate.dto.PropertyLocationDTO;
import org.example.crimerate.entity.CrimeAreaStats;
import org.example.crimerate.repository.CrimeAreaStatsRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrimeStatsService {

    private static final String SOURCE = "OSLO_STATBANK_KRI002";

    private static final double RATE_AT_ZERO_SCORE = 200.0;

    private final OsloStatbankClient statbankClient;
    private final CrimeAreaStatsRepo crimeAreaStatsRepo;
    private final PropertyClient propertyClient;

    public Optional<CrimeRateResponseDTO> getForProperty(int year) {
        PropertyLocationDTO location = propertyClient.getPropertyLocation();
        if (location == null || location.getBydel() == null) {
            log.warn("Ingen bydel tilgjengelig fra property");
            return Optional.empty();
        }
        return getBydel(location.getBydel(), year);
    }

    @Transactional
    public List<CrimeAreaStats> getStats(int year) {
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

    public List<CrimeAreaStats> buildStats(int year) {
        Map<String, Integer> crimeCounts = statbankClient.getCrimeCounts(year);
        Map<String, Integer> population = statbankClient.getPopulation(year);

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

    public List<CrimeRateResponseDTO> getAllBydeler(int year) {
        return getStats(year).stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<CrimeRateResponseDTO> getBydel(String name, int year) {
        return OsloBydel.fromName(name)
                .flatMap(bydel -> getStats(year).stream()
                        .filter(stats -> stats.getAreaCode().equals(bydel.getCode()))
                        .findFirst())
                .map(this::toResponse);
    }

    private CrimeRateResponseDTO toResponse(CrimeAreaStats stats) {
        return new CrimeRateResponseDTO(
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
