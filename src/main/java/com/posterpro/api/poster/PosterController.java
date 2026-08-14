package com.posterpro.api.poster;

import com.posterpro.api.download.DownloadFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class PosterController {

    private final PosterService posterService;

    @PostMapping("/{templateId}/generate")
    public ResponseEntity<PosterResponse> generate(
            @PathVariable Long templateId,
            @RequestParam(defaultValue = "PNG") DownloadFormat format,
            @RequestBody(required = false) PosterGenerateRequest request) {
        PosterGenerateRequest body = request != null ? request : new PosterGenerateRequest();
        return ResponseEntity.ok(posterService.generate(templateId, currentEmail(), body, format));
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
