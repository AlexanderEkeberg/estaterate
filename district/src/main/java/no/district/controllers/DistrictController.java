package no.district.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.district.services.DistrictService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/districts")
public class DistrictController {

    private final DistrictService districtService;

    @GetMapping
    public ResponseEntity<String> getDistrict() {
        String result = districtService.getDistrictData();
        return ResponseEntity.ok("Received from Property Service:" + result);
    }

}
