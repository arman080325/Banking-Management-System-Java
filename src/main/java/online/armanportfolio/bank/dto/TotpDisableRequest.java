package online.armanportfolio.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TotpDisableRequest(
        @NotBlank String password,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Enter the 6-digit code") String code
) {}
