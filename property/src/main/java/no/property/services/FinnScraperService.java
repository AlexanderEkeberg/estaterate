package no.property.services;

import no.property.dto.PropertyLocationDTO;
import no.property.dto.ScrapeResponseDTO;

public interface FinnScraperService {
    ScrapeResponseDTO scrape(String url);
    PropertyLocationDTO getLatest();
}
