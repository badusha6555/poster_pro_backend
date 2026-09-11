package com.posterpro.api.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> me() {
        return ResponseEntity.ok(userService.getProfile(currentEmail()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserProfileDto> updateMe(@RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(currentEmail(), request));
    }

    @PostMapping(value = "/me/logo", consumes = "multipart/form-data")
    public ResponseEntity<UserProfileDto> uploadLogo(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(userService.uploadLogo(currentEmail(), file));
    }

    @DeleteMapping("/me/logo")
    public ResponseEntity<UserProfileDto> deleteLogo() {
        return ResponseEntity.ok(userService.deleteLogo(currentEmail()));
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
