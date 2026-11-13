package no.favourites.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "favourites")
@Getter
@NoArgsConstructor
public class Favourite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "analysis_id", nullable = false)
    private UUID analysisId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Favourite(String userId, UUID analysisId) {
        this.userId = userId;
        this.analysisId = analysisId;
        this.createdAt = Instant.now();
    }
}
