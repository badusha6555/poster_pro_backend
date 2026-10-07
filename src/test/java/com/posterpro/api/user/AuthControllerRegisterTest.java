package com.posterpro.api.user;

import com.posterpro.api.common.GlobalExceptionHandler;
import com.posterpro.api.config.JwtAuthFilter;
import com.posterpro.api.config.JwtService;
import com.posterpro.api.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Mocked repository on purpose: these tests must never touch a real database.
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class})
class AuthControllerRegisterTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsService userDetailsService;

    private static String body(String email) {
        return """
                {"email":"%s","password":"password123","shopName":"Test Jewellers"}
                """.formatted(email);
    }


    @Test
    void registersNewUser() throws Exception {
        when(jwtService.generateToken("new@example.com")).thenReturn("jwt-token");

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("new@example.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void normalisesEmailBeforeSaving() throws Exception {
        when(jwtService.generateToken("mixed@example.com")).thenReturn("jwt-token");

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("  Mixed@Example.COM ")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("mixed@example.com"));
        verify(userRepository).existsByEmail("mixed@example.com");
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("dup@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsSameEmailInDifferentCase() throws Exception {
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("DUP@Example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void returnsConflictWhenConcurrentRegistrationWinsTheRace() throws Exception {
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("users_email_key"));

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("race@example.com")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@example.com\",\"password\":\"password123\",\"shopName\":\"Tru"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void rejectsInvalidInputWithFieldErrors() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"short\",\"shopName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
