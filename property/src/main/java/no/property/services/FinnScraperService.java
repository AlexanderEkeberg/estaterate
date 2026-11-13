package no.property.services;

import no.property.dto.PropertyDetailsDTO;
import no.property.dto.ScrapeResponseDTO;

import java.util.Optional;

public interface FinnScraperService {
    ScrapeResponseDTO scrape(String url);
    Optional<PropertyDetailsDTO> getDetails(String url);
}
