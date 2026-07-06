package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.BillPayment;
import java.math.BigDecimal;
import java.time.Instant;

public record BillPaymentResponse(
        Long id, Long accountNumber, String category, String consumer,
        BigDecimal amount, String reference, Instant createdAt
) {
    public static BillPaymentResponse from(BillPayment b) {
        return new BillPaymentResponse(b.getId(), b.getAccount().getAccountNumber(), b.getCategory(),
                b.getConsumer(), b.getAmount(), b.getReference(), b.getCreatedAt());
    }
}
