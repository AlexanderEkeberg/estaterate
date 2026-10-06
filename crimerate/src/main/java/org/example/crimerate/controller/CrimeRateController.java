package org.example.crimerate.controller;

import lombok.RequiredArgsConstructor;
import org.example.crimerate.dto.CrimeRateResponseDTO;
import org.example.crimerate.service.CrimeStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crimerate")
@RequiredArgsConstructor
public class CrimeRateController {

    private static final int FIRST_YEAR = 2017;
    private static final int LAST_YEAR = 2024;

    private final CrimeStatsService crimeStatsService;

    @GetMapping
    public ResponseEntity<CrimeRateResponseDTO> getForProperty(
            @RequestParam(defaultValue = "2024") int year) {
        if (!isValidYear(year)) {
            return ResponseEntity.badRequest().build();
        }
        return crimeStatsService.getForProperty(year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/bydeler")
    public ResponseEntity<List<CrimeRateResponseDTO>> getAllBydeler(
            @RequestParam(defaultValue = "2024") int year) {
        if (!isValidYear(year)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(crimeStatsService.getAllBydeler(year));
    }

    @GetMapping("/bydeler/{name}")
    public ResponseEntity<CrimeRateResponseDTO> getBydel(
            @PathVariable String name,
            @RequestParam(defaultValue = "2024") int year) {
        if (!isValidYear(year)) {
            return ResponseEntity.badRequest().build();
        }
        return crimeStatsService.getBydel(name, year)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean isValidYear(int year) {
        return year >= FIRST_YEAR && year <= LAST_YEAR;
    }
}
