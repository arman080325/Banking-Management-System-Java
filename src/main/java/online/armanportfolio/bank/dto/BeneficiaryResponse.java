package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.Beneficiary;
import java.time.Instant;

public record BeneficiaryResponse(Long id, String name, Long accountNumber, String nickname, Instant createdAt) {
    public static BeneficiaryResponse from(Beneficiary b) {
        return new BeneficiaryResponse(b.getId(), b.getName(), b.getAccountNumber(), b.getNickname(), b.getCreatedAt());
    }
}
