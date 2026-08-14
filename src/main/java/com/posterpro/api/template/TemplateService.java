package com.posterpro.api.template;

import com.posterpro.api.category.Category;
import com.posterpro.api.common.PageResponse;
import com.posterpro.api.common.PaginationUtil;
import com.posterpro.api.user.FavoriteRepository;
import com.posterpro.api.user.User;
import com.posterpro.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<TemplateSummaryDto> list(Long categoryId, String search, int page, int size, String email) {
        User user = findUser(email);
        String normalizedSearch = StringUtils.hasText(search) ? search.trim() : "";
        Pageable pageable = PaginationUtil.toPageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Template> result = templateRepository.search(categoryId, normalizedSearch, pageable);
        Set<Long> favoriteTemplateIds = favoriteRepository.findTemplateIdsByUserId(user.getId());

        var content = result.getContent().stream()
                .map(t -> toSummaryDto(t, favoriteTemplateIds.contains(t.getId())))
                .toList();

        return new PageResponse<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public TemplateDetailDto getById(Long id, String email) {
        User user = findUser(email);
        Template template = templateRepository.findById(id)
                .filter(Template::getIsActive)
                .orElseThrow(() -> new EntityNotFoundException("Template not found: " + id));
        boolean favorited = favoriteRepository.existsByIdUserIdAndIdTemplateId(user.getId(), id);
        return toDetailDto(template, favorited);
    }

    public TemplateSummaryDto toSummaryDto(Template t, boolean favorited) {
        Category category = t.getCategory();
        return new TemplateSummaryDto(
                t.getId(),
                t.getTitle(),
                t.getThumbnailUrl(),
                t.getPrice(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                t.getPlanTierMin() != null ? t.getPlanTierMin().name() : null,
                t.getIsFestival(),
                t.getFestivalDate(),
                favorited
        );
    }

    private TemplateDetailDto toDetailDto(Template t, boolean favorited) {
        Category category = t.getCategory();
        return new TemplateDetailDto(
                t.getId(),
                t.getTitle(),
                t.getThumbnailUrl(),
                t.getPrice(),
                category != null ? category.getId() : null,
                category != null ? category.getName() : null,
                t.getPlanTierMin() != null ? t.getPlanTierMin().name() : null,
                t.getIsFestival(),
                t.getFestivalDate(),
                favorited,
                t.getSchemaJson(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }
}
