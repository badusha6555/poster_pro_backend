package com.posterpro.api.goldrate;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gold-rates")
@RequiredArgsConstructor
public class GoldRateController {

    private final GoldRateService goldRateService;

    @GetMapping
    public ResponseEntity<GoldRateDto> get() {
        return ResponseEntity.ok(goldRateService.getProfile(currentEmail()));
    }

    @PutMapping
    public ResponseEntity<GoldRateDto> update(@RequestBody GoldRateDto dto) {
        return ResponseEntity.ok(goldRateService.updateProfile(currentEmail(), dto));
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
