package com.posterpro.api.goldrate;

import com.posterpro.api.user.User;
import com.posterpro.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GoldRateService {

    private final GoldRateRepository goldRateRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public GoldRateDto getProfile(String email) {
        User user = findUser(email);
        return goldRateRepository.findByUser(user)
                .map(this::toDto)
                .orElseGet(GoldRateDto::new);
    }

    @Transactional
    public GoldRateDto updateProfile(String email, GoldRateDto dto) {
        User user = findUser(email);
        GoldRateProfile profile = goldRateRepository.findByUser(user)
                .orElseGet(() -> {
                    GoldRateProfile p = new GoldRateProfile();
                    p.setUser(user);
                    return p;
                });
        profile.setRate22k916(dto.getRate22k916());
        profile.setRate18k(dto.getRate18k());
        profile.setRate14k(dto.getRate14k());
        profile.setRate9k(dto.getRate9k());
        profile.setUpdatedAt(LocalDateTime.now());
        return toDto(goldRateRepository.save(profile));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + email));
    }

    private GoldRateDto toDto(GoldRateProfile p) {
        GoldRateDto dto = new GoldRateDto();
        dto.setRate22k916(p.getRate22k916());
        dto.setRate18k(p.getRate18k());
        dto.setRate14k(p.getRate14k());
        dto.setRate9k(p.getRate9k());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }
}
