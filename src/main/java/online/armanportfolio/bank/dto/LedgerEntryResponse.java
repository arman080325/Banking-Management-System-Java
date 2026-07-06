package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.LedgerEntry;
import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryResponse(
        Long id, String type, BigDecimal amount, BigDecimal balanceAfter,
        String description, String reference, Instant createdAt
) {
    public static LedgerEntryResponse from(LedgerEntry e) {
        return new LedgerEntryResponse(e.getId(), e.getType().name(), e.getAmount(),
                e.getBalanceAfter(), e.getDescription(), e.getReference(), e.getCreatedAt());
    }
}
