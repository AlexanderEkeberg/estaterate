package no.favourites.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;
import java.util.TreeMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalysisSummary(String status, Map<String, Category> categories) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Category(Integer score, Object data) {
    }

    public Map<String, Integer> scores() {
        Map<String, Integer> scores = new TreeMap<>();
        if (categories != null) {
            categories.forEach((name, category) -> scores.put(name, category == null ? null : category.score()));
        }
        return scores;
    }

    public String address() {
        return propertyField("address") instanceof String address && !address.isBlank() ? address : null;
    }

    public Long askingPrice() {
        return propertyField("askingPrice") instanceof Number price ? price.longValue() : null;
    }

    public Integer usableArea() {
        return propertyField("usableArea") instanceof Number area ? area.intValue() : null;
    }

    private Object propertyField(String name) {
        Category property = categories == null ? null : categories.get("PROPERTY");
        return property != null && property.data() instanceof Map<?, ?> data ? data.get(name) : null;
    }
}
