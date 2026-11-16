package no.favourites.client;

import lombok.extern.slf4j.Slf4j;
import no.favourites.exception.AggregatorUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class AggregatorClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AggregatorClient(RestTemplate aggregatorRestTemplate, @Value("${aggregator.url}") String baseUrl) {
        this.restTemplate = aggregatorRestTemplate;
        this.baseUrl = baseUrl;
    }

    public Optional<AnalysisSummary> getAnalysis(UUID analysisId) {
        try {
            return Optional.ofNullable(restTemplate.getForObject(
                    baseUrl + "/api/analysis/{id}", AnalysisSummary.class, analysisId));
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (RestClientException | IllegalStateException e) {
            log.warn("Could not contact aggregator for {}: {}", analysisId, e.getMessage());
            throw new AggregatorUnavailableException(e);
        }
    }
}
