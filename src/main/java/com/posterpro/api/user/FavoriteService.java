package com.posterpro.api.user;

import com.posterpro.api.common.PageResponse;
import com.posterpro.api.common.PaginationUtil;
import com.posterpro.api.template.Template;
import com.posterpro.api.template.TemplateRepository;
import com.posterpro.api.template.TemplateService;
import com.posterpro.api.template.TemplateSummaryDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TemplateRepository templateRepository;
    private final TemplateService templateService;

    @Transactional
    public void addFavorite(String email, Long templateId) {
        User user = findUser(email);
        FavoriteId id = new FavoriteId(user.getId(), templateId);
        if (favoriteRepository.existsById(id)) {
            return;
        }
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new EntityNotFoundException("Template not found: " + templateId));

        Favorite favorite = new Favorite();
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setTemplate(template);
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(String email, Long templateId) {
        User user = findUser(email);
        FavoriteId id = new FavoriteId(user.getId(), templateId);
        if (favoriteRepository.existsById(id)) {
            favoriteRepository.deleteById(id);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<TemplateSummaryDto> listFavorites(String email, int page, int size) {
        User user = findUser(email);
        Pageable pageable = PaginationUtil.toPageable(page, size, org.springframework.data.domain.Sort.unsorted());
        Page<Favorite> result = favoriteRepository.findByUserId(user.getId(), pageable);

        var content = result.getContent().stream()
                .map(f -> templateService.toSummaryDto(f.getTemplate(), true))
                .toList();

        return new PageResponse<>(content, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }
}
