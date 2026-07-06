package online.armanportfolio.bank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BeneficiaryRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Long accountNumber,
        @Size(max = 60) String nickname
) {}
