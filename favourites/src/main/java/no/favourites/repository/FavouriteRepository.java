package no.favourites.repository;

import no.favourites.entity.Favourite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FavouriteRepository extends JpaRepository<Favourite, Long> {

    List<Favourite> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Favourite> findByUserIdAndAnalysisId(String userId, UUID analysisId);
}
