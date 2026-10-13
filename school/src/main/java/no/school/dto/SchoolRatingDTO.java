package no.school.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SchoolRatingDTO {
    private int score;
    private String nearestSchool;
    private double distanceKm;
}
