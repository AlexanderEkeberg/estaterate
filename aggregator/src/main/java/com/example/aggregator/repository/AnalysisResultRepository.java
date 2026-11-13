package com.example.aggregator.repository;

import com.example.aggregator.entity.AnalysisResult;
import com.example.aggregator.entity.AnalysisResultId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, AnalysisResultId> {
    List<AnalysisResult> findByIdAnalysisIdOrderByIdCategoryAsc(String analysisId);
    Optional<AnalysisResult> findByIdAnalysisIdAndIdCategory(String analysisId, String category);
}