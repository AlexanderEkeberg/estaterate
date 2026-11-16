package no.favourites.dto;

import no.favourites.client.AnalysisSummary;
import no.favourites.entity.Favourite;

import java.time.Instant;
import java.util.Map;

public record FavouriteDTO(
        String analysisId,
        Instant savedAt,
        boolean available,
        String status,
        Map<String, Integer> scores,
        String address,
        Long askingPrice,
        Integer usableArea
) {

    public static FavouriteDTO from(Favourite favourite, AnalysisSummary summary) {
        String analysisId = favourite.getAnalysisId().toString();
        if (summary == null) {
            return new FavouriteDTO(analysisId, favourite.getCreatedAt(), false, null, Map.of(), null, null, null);
        }
        return new FavouriteDTO(analysisId, favourite.getCreatedAt(), true,
                summary.status(), summary.scores(), summary.address(),
                summary.askingPrice(), summary.usableArea());
    }
}
