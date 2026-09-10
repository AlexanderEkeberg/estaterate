package no.property.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/properties") 
public class PropertyController {

    @GetMapping
    public ResponseEntity<String> findAll() {
        return ResponseEntity.ok("TEST");
    }

}
