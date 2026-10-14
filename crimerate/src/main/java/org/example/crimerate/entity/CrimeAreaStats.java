package org.example.crimerate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "crime_area_stats", uniqueConstraints = @UniqueConstraint(columnNames = {"area_code", "area_type", "year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrimeAreaStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "area_code", nullable = false, length = 10)
    private String areaCode;

    @Column(name = "area_name", nullable = false, length = 100)
    private String areaName;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_type", nullable = false, length = 20)
    private AreaType areaType;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "crime_count", nullable = false)
    private Integer crimeCount;

    @Column(nullable = false)
    private Integer population;

    @Column(name = "rate_per_1000", nullable = false)
    private Double ratePer1000;

    @Column(nullable = false)
    private Double score;

    public enum AreaType {
        BYDEL
    }
}
