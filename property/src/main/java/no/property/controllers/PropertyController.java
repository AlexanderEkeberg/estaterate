package no.property.controllers;

import lombok.RequiredArgsConstructor;
import no.property.dto.PropertyLocationDTO;
import no.property.dto.ScrapeRequestDTO;
import no.property.dto.ScrapeResponseDTO;
import no.property.services.FinnScraperService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @PostMapping("/scrape")
    public ResponseEntity<ScrapeResponseDTO> scrape(@RequestBody ScrapeRequestDTO request) {
        ScrapeResponseDTO result = finnScraperService.scrape(request.getUrl());
        return ResponseEntity.ok(result);
    }

    // Midlertidig: brukes internt av crimerate og school til de går over til Kafka. Ikke for frontend.
    @GetMapping
    public ResponseEntity<PropertyLocationDTO> getLatest() {
        PropertyLocationDTO latest = finnScraperService.getLatest();
        if (latest == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(latest);
    }
    @GetMapping("/secured")
    public ResponseEntity<String> secured(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return ResponseEntity.ok("Secured endpoint: token for user " + username + " is valid");
    }
}
