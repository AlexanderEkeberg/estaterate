package org.example.crimerate.client;

import lombok.extern.slf4j.Slf4j;
import org.example.crimerate.dto.StatbankResponseDTO;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OsloStatbankClient {

    private static final String BASE_URL = "https://statistikkbanken.oslo.kommune.no/statbank/api/v1/no/db1";
    private static final String KRI002_URL = BASE_URL + "/Kriminalitet/KRI002.px";
    private static final String BEF005_URL = BASE_URL + "/Befolkning/Folkemengde/OK-BEF005.px";

    private static final int FIRST_YEAR = 2017;

    private static final List<String> BYDEL_CODES =
            List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15");

    private final RestTemplate restTemplate;

    public OsloStatbankClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public Map<String, Integer> getCrimeCounts(int year) {
        Map<String, Object> query = Map.of(
                "query", List.of(
                        selection("gjerningssted", BYDEL_CODES),
                        selection("lovbruddsgruppe", List.of("0")),
                        selection("år", List.of(yearIndex(year)))
                ),
                "response", Map.of("format", "json")
        );
        return fetch(KRI002_URL, query, 0);
    }

    public Map<String, Integer> getPopulation(int year) {
        Map<String, Object> query = Map.of(
                "query", List.of(
                        selection("år", List.of(yearIndex(year))),
                        selection("bosted", BYDEL_CODES),
                        selection("aldersgruppe", List.of("0"))
                ),
                "response", Map.of("format", "json")
        );
        return fetch(BEF005_URL, query, 1);
    }

    private Map<String, Integer> fetch(String url, Map<String, Object> query, int bydelKeyIndex) {
        StatbankResponseDTO response = restTemplate.postForObject(url, query, StatbankResponseDTO.class);
        Map<String, Integer> result = new HashMap<>();
        if (response == null || response.data() == null) {
            log.warn("Tomt svar fra statistikkbanken: {}", url);
            return result;
        }
        for (StatbankResponseDTO.DataRow row : response.data()) {
            String bydelCode = row.key().get(bydelKeyIndex);
            String value = row.values().getFirst();
            if (value.matches("\\d+")) {
                result.put(bydelCode, Integer.parseInt(value));
            }
        }
        return result;
    }

    private Map<String, Object> selection(String code, List<String> values) {
        return Map.of("code", code, "selection", Map.of("filter", "item", "values", values));
    }

    private String yearIndex(int year) {
        return String.valueOf(year - FIRST_YEAR);
    }
}
