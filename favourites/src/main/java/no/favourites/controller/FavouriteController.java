package no.favourites.controller;

import lombok.RequiredArgsConstructor;
import no.favourites.dto.AddFavouriteRequest;
import no.favourites.dto.FavouriteDTO;
import no.favourites.dto.SaveResult;
import no.favourites.service.FavouriteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
public class FavouriteController {

    private final FavouriteService favouriteService;

    @GetMapping
    public List<FavouriteDTO> list(@AuthenticationPrincipal Jwt jwt) {
        return favouriteService.list(jwt.getSubject());
    }

    @PostMapping
    public ResponseEntity<FavouriteDTO> save(@AuthenticationPrincipal Jwt jwt, @RequestBody AddFavouriteRequest request) {
        SaveResult result = favouriteService.save(jwt.getSubject(), request.analysisId());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.favourite());
    }

    @DeleteMapping("/{analysisId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable String analysisId) {
        favouriteService.remove(jwt.getSubject(), analysisId);
        return ResponseEntity.noContent().build();
    }
}
