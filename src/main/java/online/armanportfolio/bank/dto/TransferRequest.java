package online.armanportfolio.bank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TransferRequest(
        @NotNull Long fromAccountNumber,
        @NotNull Long toAccountNumber,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive")
        @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[0-9]{4,6}$") String pin,
        /** Only required for transfers at/above the high-value threshold on a 2FA-enabled account. */
        String totpCode
) {}
