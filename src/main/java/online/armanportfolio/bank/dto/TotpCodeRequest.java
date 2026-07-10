package online.armanportfolio.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TotpCodeRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "Enter the 6-digit code") String code
) {}
