package com.posterpro.api.template;

import com.posterpro.api.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<PageResponse<TemplateSummaryDto>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(templateService.list(categoryId, search, page, size, currentEmail()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateDetailDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(templateService.getById(id, currentEmail()));
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
