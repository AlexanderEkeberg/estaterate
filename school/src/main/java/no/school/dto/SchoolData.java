package no.school.dto;

public record SchoolData(
        String nearestSchool,
        double distanceKm,
        DataSource dataSource
) {
}
