package no.property.controllers;

import lombok.RequiredArgsConstructor;
import no.property.dto.PropertyDetailsDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/details")
    public ResponseEntity<PropertyDetailsDTO> details(@RequestParam String url) {
        return finnScraperService.getDetails(url)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/secured")
    public ResponseEntity<String> secured(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        return ResponseEntity.ok("Secured endpoint: token for user " + username + " is valid");
    }
}
