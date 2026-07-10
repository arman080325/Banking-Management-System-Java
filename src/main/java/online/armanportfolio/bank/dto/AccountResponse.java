package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.Account;
import java.math.BigDecimal;

public record AccountResponse(Long id, Long accountNumber, String holderName, BigDecimal balance, boolean frozen) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getAccountNumber(), a.getHolderName(), a.getBalance(), a.isFrozen());
    }
}
