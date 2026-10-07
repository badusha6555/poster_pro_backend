package com.posterpro.api.user;

import com.posterpro.api.config.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        String email = User.normalizeEmail(req.getEmail());
        if (userRepository.existsByEmail(email)) {
            return emailTaken(email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setShopName(req.getShopName());
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            // Lost a race with a concurrent registration of the same email.
            return emailTaken(email);
        }

        String token = jwtService.generateToken(user.getEmail());
        log.info("User registered: {}", user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, user.getEmail(), user.getShopName()));
    }

    private ResponseEntity<?> emailTaken(String email) {
        log.warn("Registration failed for {}: email already registered", email);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", "Email is already registered"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(User.normalizeEmail(req.getEmail()), req.getPassword())
            );
            String email = auth.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
            String token = jwtService.generateToken(email);
            log.info("Login succeeded for {}", email);
            return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getShopName()));
        } catch (BadCredentialsException e) {
            log.warn("Login failed for {}: invalid credentials", req.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }
}
