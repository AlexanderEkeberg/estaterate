package org.example.crimerate.repository;

import org.example.crimerate.entity.CrimeAreaStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrimeAreaStatsRepo extends JpaRepository<CrimeAreaStats, Long> {

    List<CrimeAreaStats> findByAreaTypeAndYear(CrimeAreaStats.AreaType areaType, Integer year);
}
