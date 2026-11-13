package com.example.aggregator.service;

import com.example.aggregator.entity.AnalysisResult;
import com.example.aggregator.entity.CategoryStatus;
import com.example.aggregator.kafka.ScoreEvent;
import com.example.aggregator.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.aggregator.client.PropertyClient;
import com.example.aggregator.dto.PropertyDetailsDTO;
import com.example.aggregator.kafka.ScoreStatus;
import org.springframework.web.client.RestClientException;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private static final List<String> KNOWN_CATEGORIES = List.of("WEATHER", "CRIME", "SCHOOL", "PROPERTY");

    private final PropertyClient propertyClient;
    private final AnalysisResultRepository repository;

    @Override
    public void ensureAnalysisExists(String analysisId) {
        if (analysisId == null || analysisId.isBlank()) return;

        for (String category : KNOWN_CATEGORIES) {
            repository.findByIdAnalysisIdAndIdCategory(analysisId, category)
                    .orElseGet(() -> repository.save(AnalysisResult.pending(analysisId, category)));
        }
    }

    @Override
    public void processScoreEvent(ScoreEvent event) {
        if (event == null || event.analysisId() == null || event.category() == null) return;
        ensureAnalysisExists(event.analysisId());

        AnalysisResult result = repository.findByIdAnalysisIdAndIdCategory(event.analysisId(), event.category())
                .orElseGet(() -> AnalysisResult.pending(event.analysisId(), event.category()));

        result.apply(event);
        repository.save(result);
    }

    @Override
    public AnalysisStatusResponse getAnalysis(String analysisId) {
        List<AnalysisResult> rows = repository.findByIdAnalysisIdOrderByIdCategoryAsc(analysisId);
        if (rows.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Analysis not found: " + analysisId);
        }

        Map<String, CategoryStatusResponse> categories = new LinkedHashMap<>();
        for (AnalysisResult row : rows) {
            categories.put(row.getId().getCategory(), new CategoryStatusResponse(
                    row.getStatus().name(),
                    row.getScore(),
                    row.getData(),
                    row.getError()
            ));
        }

        boolean isDone = rows.stream().allMatch(row -> row.getStatus() != CategoryStatus.PENDING);

        return new AnalysisStatusResponse(analysisId, isDone ? "DONE" : "RUNNING", categories);
    }

    @Override
    public void processPropertySubmitted(String analysisId, String url) {
        if (analysisId == null || analysisId.isBlank()) return;

        ensureAnalysisExists(analysisId);

        if (url == null || url.isBlank()) {
            processScoreEvent(new ScoreEvent(
                    analysisId,
                    "PROPERTY",
                    ScoreStatus.FAILED,
                    null,
                    null,
                    "Property URL is missing"
            ));
            return;
        }

        try {
            var details = propertyClient.getDetails(url);
            if (details.isPresent()) {
                PropertyDetailsDTO d = details.get();
                LinkedHashMap<String, Object> data = new LinkedHashMap<>();
                data.put("address", d.address());
                data.put("postalCode", d.postalCode());
                data.put("askingPrice", d.askingPrice());
                data.put("usableArea", d.usableArea());

                processScoreEvent(new ScoreEvent(
                        analysisId,
                        "PROPERTY",
                        ScoreStatus.SUCCESS,
                        null,
                        data,
                        null
                ));
            } else {
                processScoreEvent(new ScoreEvent(
                        analysisId,
                        "PROPERTY",
                        ScoreStatus.FAILED,
                        null,
                        null,
                        "Property details not found for url: " + url
                ));
            }
        } catch (RestClientException e) {
            processScoreEvent(new ScoreEvent(
                    analysisId,
                    "PROPERTY",
                    ScoreStatus.FAILED,
                    null,
                    null,
                    "Could not fetch property details: " + e.getMessage()
            ));
        }
    }
}