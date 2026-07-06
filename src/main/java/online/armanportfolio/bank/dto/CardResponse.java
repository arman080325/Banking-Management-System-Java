package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.Card;
import java.math.BigDecimal;
import java.time.Instant;

public record CardResponse(
        Long accountNumber, String holderName, String network,
        int expiryMonth, int expiryYear, boolean frozen,
        boolean contactlessEnabled, boolean onlineEnabled,
        BigDecimal dailyLimit, Instant replacementRequestedAt
) {
    public static CardResponse from(Card c) {
        return new CardResponse(
                c.getAccount().getAccountNumber(), c.getAccount().getHolderName(), c.getNetwork(),
                c.getExpiryMonth(), c.getExpiryYear(), c.isFrozen(),
                c.isContactlessEnabled(), c.isOnlineEnabled(),
                c.getDailyLimit(), c.getReplacementRequestedAt());
    }
}
