package online.armanportfolio.bank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record BillPaymentRequest(
        @NotNull Long accountNumber,
        @NotBlank @Size(max = 60) String category,
        @NotBlank @Size(max = 60) String consumer,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive")
        @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[0-9]{4,6}$") String pin
) {}
