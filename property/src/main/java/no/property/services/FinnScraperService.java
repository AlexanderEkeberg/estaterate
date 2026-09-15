package no.property.services;

import no.property.dto.PropertyLocationDTO;

public interface FinnScraperService {
    PropertyLocationDTO scrape(String url);
    PropertyLocationDTO getLatest();
}
