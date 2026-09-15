package no.property.controllers;

import lombok.RequiredArgsConstructor;
import no.property.dto.PropertyLocationDTO;
import no.property.dto.ScrapeRequestDTO;
import no.property.services.FinnScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final FinnScraperService finnScraperService;

    // TODO: valider at request.getUrl() ikke er tom/ugyldig
    //TODO: håndter feil fra scraping
    @PostMapping("/scrape")
    public ResponseEntity<PropertyLocationDTO> scrape(@RequestBody ScrapeRequestDTO request) {
        PropertyLocationDTO result = finnScraperService.scrape(request.getUrl());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<PropertyLocationDTO> getLatest() {
        PropertyLocationDTO latest = finnScraperService.getLatest();
        if (latest == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(latest);
    }
}
