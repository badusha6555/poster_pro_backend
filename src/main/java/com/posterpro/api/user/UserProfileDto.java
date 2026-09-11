package com.posterpro.api.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Response shape for GET/PATCH /api/users/me and the logo endpoints.
 * Intentionally excludes passwordHash. Also excludes plan tier — that isn't
 * actually a User column (it lives on Subscription) and is out of scope for
 * this profile endpoint.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String email;
    private String shopName;
    private String shopPhone;
    private String shopAddress;
    private String businessType;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
