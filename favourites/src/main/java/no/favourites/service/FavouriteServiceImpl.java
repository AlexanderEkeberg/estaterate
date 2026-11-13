package no.favourites.service;

import lombok.RequiredArgsConstructor;
import no.favourites.client.AggregatorClient;
import no.favourites.client.AnalysisSummary;
import no.favourites.dto.FavouriteDTO;
import no.favourites.dto.SaveResult;
import no.favourites.entity.Favourite;
import no.favourites.exception.AggregatorUnavailableException;
import no.favourites.exception.AnalysisNotFoundException;
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
    private final AggregatorClient aggregatorClient;

    @Override
    public List<FavouriteDTO> list(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(favourite -> FavouriteDTO.from(favourite, summaryOrNull(favourite.getAnalysisId())))
                .toList();
    }

    @Override
    public SaveResult save(String userId, String analysisId) {
        UUID id = parse(analysisId);
        AnalysisSummary summary = aggregatorClient.getAnalysis(id).orElseThrow(AnalysisNotFoundException::new);
        return repository.findByUserIdAndAnalysisId(userId, id)
                .map(existing -> new SaveResult(FavouriteDTO.from(existing, summary), false))
                .orElseGet(() -> insert(userId, id, summary));
    }

    @Override
    @Transactional
    public void remove(String userId, String analysisId) {
        repository.findByUserIdAndAnalysisId(userId, parse(analysisId))
                .ifPresent(repository::delete);
    }

    private SaveResult insert(String userId, UUID analysisId, AnalysisSummary summary) {
        try {
            Favourite saved = repository.saveAndFlush(new Favourite(userId, analysisId));
            return new SaveResult(FavouriteDTO.from(saved, summary), true);
        } catch (DataIntegrityViolationException e) {
            Favourite existing = repository.findByUserIdAndAnalysisId(userId, analysisId).orElseThrow();
            return new SaveResult(FavouriteDTO.from(existing, summary), false);
        }
    }

    private AnalysisSummary summaryOrNull(UUID analysisId) {
        try {
            return aggregatorClient.getAnalysis(analysisId).orElse(null);
        } catch (AggregatorUnavailableException e) {
            return null;
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
