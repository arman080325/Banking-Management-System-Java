package online.armanportfolio.bank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Credit or debit against one of the caller's own accounts. */
public record MoneyRequest(
        @NotNull Long accountNumber,
        @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive")
        @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[0-9]{4,6}$") String pin
) {}
