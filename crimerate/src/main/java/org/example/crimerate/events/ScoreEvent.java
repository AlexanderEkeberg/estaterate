package org.example.crimerate.events;

public record ScoreEvent(
        String analysisId,
        String category,
        ScoreStatus status,
        Integer score,
        Object data,
        String error
) {
    public static ScoreEvent success(String analysisId, String category, Integer score, Object data) {
        return new ScoreEvent(analysisId, category, ScoreStatus.SUCCESS, score, data, null);
    }

    public static ScoreEvent failed(String analysisId, String category, String error) {
        return new ScoreEvent(analysisId, category, ScoreStatus.FAILED, null, null, error);
    }
}
