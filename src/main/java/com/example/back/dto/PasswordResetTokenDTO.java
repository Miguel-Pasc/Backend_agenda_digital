package com.example.back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class PasswordResetTokenDTO {

    @Setter
    @Getter
    public static class RecuperarPasswordRequest {  // ← agregar static
        @NotBlank
        @Email
        private String correo;

    }

    @Setter
    @Getter
    public static class ResetPasswordRequest {      // ← agregar static
        @NotBlank
        private String token;
        @NotBlank
        @Size(min = 8)
        private String nuevaPassword;

    }
}