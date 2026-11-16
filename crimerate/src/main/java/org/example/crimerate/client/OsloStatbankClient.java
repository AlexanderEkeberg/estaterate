package org.example.crimerate.client;

import lombok.extern.slf4j.Slf4j;
import org.example.crimerate.dto.StatbankResponseDTO;
import org.example.crimerate.service.OsloBydel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OsloStatbankClient {

    private static final String KRI002_PATH = "/Kriminalitet/KRI002.px";
    private static final String BEF005_PATH = "/Befolkning/Folkemengde/OK-BEF005.px";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private static final int FIRST_YEAR = 2017;
    public static final int LAST_YEAR = 2024;

    private static final List<String> BYDEL_CODES =
            Arrays.stream(OsloBydel.values()).map(OsloBydel::getCode).toList();

    private final String baseUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public OsloStatbankClient(RestTemplateBuilder builder, @Value("${statbank.url}") String baseUrl) {
        this(builder.connectTimeout(CONNECT_TIMEOUT).readTimeout(READ_TIMEOUT).build(), baseUrl);
    }

    OsloStatbankClient(RestTemplate restTemplate, String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
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
        return fetch(baseUrl + KRI002_PATH, query, 0);
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
        return fetch(baseUrl + BEF005_PATH, query, 1);
    }

    private Map<String, Integer> fetch(String url, Map<String, Object> query, int bydelKeyIndex) {
        StatbankResponseDTO response;
        try {
            response = HttpRetry.call(() -> restTemplate.postForObject(url, query, StatbankResponseDTO.class));
        } catch (RestClientException e) {
            log.atWarn()
                    .addKeyValue("event_type", "statbank_unavailable")
                    .addKeyValue("url", url)
                    .addKeyValue("reason", e.getMessage())
                    .log("Could not contact statbank");
            throw new StatbankException("Could not contact statbank", e);
        }
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new StatbankException("Statbank returned no data");
        }

        Map<String, Integer> result = new HashMap<>();
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
