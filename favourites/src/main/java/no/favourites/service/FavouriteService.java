package no.favourites.service;

import no.favourites.dto.FavouriteDTO;
import no.favourites.dto.SaveResult;

import java.util.List;

public interface FavouriteService {

    List<FavouriteDTO> list(String userId);

    SaveResult save(String userId, String analysisId);
    void remove(String userId, String analysisId);
}
