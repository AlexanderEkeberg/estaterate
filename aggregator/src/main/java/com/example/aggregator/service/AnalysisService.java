package com.example.aggregator.service;

import com.example.aggregator.kafka.ScoreEvent;


import java.util.Map;

public interface AnalysisService {

    void ensureAnalysisExists(String analysisId);

    void processScoreEvent(ScoreEvent event);

    void processPropertySubmitted(String analysisId, String url);

    AnalysisStatusResponse getAnalysis(String analysisId);

    record AnalysisStatusResponse(
            String analysisId,
            String status,
            Map<String, CategoryStatusResponse> categories
    ) {}

    record CategoryStatusResponse(
            String status,
            Integer score,
            Object data,
            String error
    ) {}
}