package online.armanportfolio.bank.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72, message = "Password must be 8–72 characters") String password,
        @NotBlank @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Enter a valid 10-digit mobile number") String phone,
        @NotNull @Past(message = "Enter a valid date of birth") LocalDate dateOfBirth,
        @NotBlank @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]$", message = "PAN must look like ABCDE1234F") String panNumber,
        @NotBlank @Size(max = 200) String address
) {}
