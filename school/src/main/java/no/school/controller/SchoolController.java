package no.school.controller;

import lombok.RequiredArgsConstructor;
import no.school.dto.SchoolRatingDTO;
import no.school.service.SchoolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/school")
@RequiredArgsConstructor

public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping("/rating")
    public ResponseEntity<SchoolRatingDTO> getRating() {
        SchoolRatingDTO rating = schoolService.getRating();
        return rating != null
                ? ResponseEntity.ok(rating)
                : ResponseEntity.status(503).build();
    }
}