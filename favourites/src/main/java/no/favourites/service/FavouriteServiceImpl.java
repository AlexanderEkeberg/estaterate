package no.favourites.service;

import lombok.RequiredArgsConstructor;
import no.favourites.dto.FavouriteDTO;
import no.favourites.dto.SaveResult;
import no.favourites.entity.Favourite;
import no.favourites.exception.InvalidAnalysisIdException;
import no.favourites.repository.FavouriteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FavouriteServiceImpl implements FavouriteService {

    private final FavouriteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<FavouriteDTO> list(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(FavouriteDTO::from)
                .toList();
    }

    @Override
    public SaveResult save(String userId, String analysisId) {
        UUID id = parse(analysisId);
        return repository.findByUserIdAndAnalysisId(userId, id)
                .map(existing -> new SaveResult(FavouriteDTO.from(existing), false))
                .orElseGet(() -> insert(userId, id));
    }

    @Override
    @Transactional
    public void remove(String userId, String analysisId) {
        repository.findByUserIdAndAnalysisId(userId, parse(analysisId))
                .ifPresent(repository::delete);
    }

    private SaveResult insert(String userId, UUID analysisId) {
        try {
            Favourite saved = repository.saveAndFlush(new Favourite(userId, analysisId));
            return new SaveResult(FavouriteDTO.from(saved), true);
        } catch (DataIntegrityViolationException e) {
            Favourite existing = repository.findByUserIdAndAnalysisId(userId, analysisId).orElseThrow();
            return new SaveResult(FavouriteDTO.from(existing), false);
        }
    }

    private UUID parse(String analysisId) {
        if (analysisId == null) {
            throw new InvalidAnalysisIdException();
        }
        try {
            UUID id = UUID.fromString(analysisId);
            if (!id.toString().equalsIgnoreCase(analysisId)) {
                throw new InvalidAnalysisIdException();
            }
            return id;
        } catch (IllegalArgumentException e) {
            throw new InvalidAnalysisIdException();
        }
    }
}
