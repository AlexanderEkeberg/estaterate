package com.example.aggregator.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScoreEvent(
        String analysisId,
        String category,
        ScoreStatus status,
        Integer score,
        Object data,
        String error
) {}