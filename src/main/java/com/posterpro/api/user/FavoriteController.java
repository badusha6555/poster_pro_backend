package com.posterpro.api.user;

import com.posterpro.api.common.PageResponse;
import com.posterpro.api.template.TemplateSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{templateId}")
    public ResponseEntity<Void> favorite(@PathVariable Long templateId) {
        favoriteService.addFavorite(currentEmail(), templateId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> unfavorite(@PathVariable Long templateId) {
        favoriteService.removeFavorite(currentEmail(), templateId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<TemplateSummaryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(favoriteService.listFavorites(currentEmail(), page, size));
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
