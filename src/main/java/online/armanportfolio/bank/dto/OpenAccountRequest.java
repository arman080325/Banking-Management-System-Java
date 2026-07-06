package online.armanportfolio.bank.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record OpenAccountRequest(
        @NotBlank @Size(max = 100) String holderName,
        @NotNull @DecimalMin(value = "0.0", message = "Opening balance can't be negative")
        @Digits(integer = 17, fraction = 2) BigDecimal openingBalance,
        @NotBlank @Pattern(regexp = "^[0-9]{4,6}$", message = "PIN must be 4–6 digits") String pin
) {}
