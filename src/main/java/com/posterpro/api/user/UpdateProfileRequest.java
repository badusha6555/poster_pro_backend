package com.posterpro.api.user;

import lombok.Getter;
import lombok.Setter;

/**
 * PATCH /api/users/me body. shopName is the only editable field the app
 * currently exposes here — left null it is unchanged; sent blank it is
 * rejected (see UserService#updateProfile). email/id/plan tier are
 * intentionally not settable through this endpoint.
 */
@Getter
@Setter
public class UpdateProfileRequest {
    private String shopName;
}
