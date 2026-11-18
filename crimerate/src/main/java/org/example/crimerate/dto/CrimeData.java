package org.example.crimerate.dto;

import java.util.List;

public record CrimeData(
        DataSource source,
        String bydel,
        int year,
        int crimeCount,
        int population,
        double ratePer1000,
        double score,
        List<CrimeYear> history
) {
}
