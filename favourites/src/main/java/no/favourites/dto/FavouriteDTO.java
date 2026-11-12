package no.favourites.dto;

import no.favourites.entity.Favourite;

import java.time.Instant;

public record FavouriteDTO(String analysisId, Instant savedAt) {

    public static FavouriteDTO from(Favourite favourite) {
        return new FavouriteDTO(favourite.getAnalysisId().toString(), favourite.getCreatedAt());
    }
}
