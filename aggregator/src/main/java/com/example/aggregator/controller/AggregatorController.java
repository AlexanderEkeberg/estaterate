package com.example.aggregator.controller;

import com.example.aggregator.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AggregatorController {

    private final AnalysisService analysisService;

    @GetMapping("/api/analysis/{analysisId}")
    public AnalysisService.AnalysisStatusResponse getAnalysis(@PathVariable String analysisId) {
        return analysisService.getAnalysis(analysisId);
    }

}