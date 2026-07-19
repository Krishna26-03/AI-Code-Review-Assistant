package com.astra.codereview.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

/**
 * Container for the small request/response records used by the auth flow.
 * Kept in one file to match the "dto" folder being a thin, flat layer.
 */
public class AuthDtos {

    public record RegisterRequest(
            @NotBlank(message = "Name is required")
            @Pattern(
                    regexp = "^[A-Za-z][A-Za-z0-9\\s'-]*$",
                    message = "Name must start with a letter and can contain only letters, numbers, spaces, hyphens, and apostrophes."
            )
            String name,

            @NotBlank
            @Email(message = "A valid email is required")
            String email,

            @NotBlank
            @Size(min = 8, message = "Password must be at least 8 characters")
            String password
    ) {}

    public record LoginRequest(
            @NotBlank @Email(message = "A valid email is required") String email,
            @NotBlank(message = "Password is required") String password
    ) {}

    public record AuthResponse(
            String token,
            String tokenType,
            Long userId,
            String name,
            String email
    ) {
        public static AuthResponse of(String token, Long userId, String name, String email) {
            return new AuthResponse(token, "Bearer", userId, name, email);
        }
    }
}
