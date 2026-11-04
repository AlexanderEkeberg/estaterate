package com.example.aggregator.entity;

import com.example.aggregator.kafka.ScoreEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.LinkedHashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "analysis_results", uniqueConstraints = @UniqueConstraint(columnNames = {"analysis_id", "category"})
)
public class AnalysisResult {

    @EmbeddedId
    private AnalysisResultId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryStatus status = CategoryStatus.PENDING;

    @Column
    private Integer score;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    private Map<String, Object> data;

    @Column(length = 1000)
    private String error;

    public AnalysisResult(String analysisId, String category) {
        this.id = new AnalysisResultId(analysisId, category);
    }

    public static AnalysisResult pending(String analysisId, String category) {
        return new AnalysisResult(analysisId, category);
    }

    public void apply(ScoreEvent event) {
        if (event == null) return;

        if (event.status() == null) {
            this.status = CategoryStatus.PENDING;
        } else {
            this.status = switch (event.status()) {
                case SUCCESS -> CategoryStatus.SUCCESS;
                case FAILED -> CategoryStatus.FAILED;
            };
        }

        this.score = event.score();
        this.data = normalizeData(event.data());
        this.error = event.error();
    }

    private Map<String, Object> normalizeData(Object data) {
        if (data == null) return null;
        if (data instanceof Map<?, ?> map) {
            Map<String, Object> normalized = new LinkedHashMap<>();
            map.forEach((k, v) -> normalized.put(String.valueOf(k), v));
            return normalized;
        }
        Map<String, Object> wrapped = new LinkedHashMap<>();
        wrapped.put("value", data);
        return wrapped;
    }
}