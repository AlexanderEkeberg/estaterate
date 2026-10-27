package no.weather.events;

// score alltid null
public record ScoreEvent(
        String analysisId,
        String category,
        ScoreStatus status,
        Integer score,
        Object data,
        String error
) {
    public static ScoreEvent success(String analysisId, String category, Object data) {
        return new ScoreEvent(analysisId, category, ScoreStatus.SUCCESS, null, data, null);
    }

    public static ScoreEvent failed(String analysisId, String category, String error) {
        return new ScoreEvent(analysisId, category, ScoreStatus.FAILED, null, null, error);
    }
}
