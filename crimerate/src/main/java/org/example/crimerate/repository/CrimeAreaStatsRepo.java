package org.example.crimerate.repository;

import org.example.crimerate.entity.CrimeAreaStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrimeAreaStatsRepo extends JpaRepository<CrimeAreaStats, Long> {

    List<CrimeAreaStats> findByAreaTypeAndYear(CrimeAreaStats.AreaType areaType, Integer year);
}
