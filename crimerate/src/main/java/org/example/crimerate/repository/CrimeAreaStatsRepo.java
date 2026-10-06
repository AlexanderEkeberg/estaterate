package org.example.crimerate.repository;

import org.example.crimerate.entity.CrimeAreaStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrimeAreaStatsRepo extends JpaRepository<CrimeAreaStats, Long> {

    Optional<CrimeAreaStats> findByAreaCodeAndAreaTypeAndYear(
            String areaCode, CrimeAreaStats.AreaType areaType, Integer year);

    Optional<CrimeAreaStats> findFirstByAreaCodeAndAreaTypeOrderByYearDesc(
            String areaCode, CrimeAreaStats.AreaType areaType);

    List<CrimeAreaStats> findByAreaTypeAndYear(CrimeAreaStats.AreaType areaType, Integer year);
}
