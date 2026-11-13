package no.favourites.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.TreeMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisSummary(String status, Map<String, Category> categories, Property property) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Category(Integer score) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Property(String address) {
    }

    public Map<String, Integer> scores() {
        Map<String, Integer> scores = new TreeMap<>();
        if (categories != null) {
            categories.forEach((name, category) -> scores.put(name, category == null ? null : category.score()));
        }
        return scores;
    }

    public String address() {
        return property == null ? null : property.address();
    }
}
