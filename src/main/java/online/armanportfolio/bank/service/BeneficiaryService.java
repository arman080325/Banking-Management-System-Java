package online.armanportfolio.bank.service;

import online.armanportfolio.bank.dto.BeneficiaryRequest;
import online.armanportfolio.bank.dto.BeneficiaryResponse;
import online.armanportfolio.bank.exception.ApiException;
import online.armanportfolio.bank.model.Beneficiary;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.AccountRepository;
import online.armanportfolio.bank.repository.BeneficiaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaries;
    private final AccountRepository accounts;

    public BeneficiaryService(BeneficiaryRepository beneficiaries, AccountRepository accounts) {
        this.beneficiaries = beneficiaries;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> mine(User owner) {
        return beneficiaries.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream().map(BeneficiaryResponse::from).toList();
    }

    @Transactional
    public BeneficiaryResponse add(User owner, BeneficiaryRequest req) {
        if (!accounts.existsByAccountNumber(req.accountNumber())) {
            throw ApiException.badRequest("No account exists with that number");
        }
        Beneficiary b = new Beneficiary();
        b.setOwner(owner);
        b.setName(req.name());
        b.setAccountNumber(req.accountNumber());
        b.setNickname(req.nickname());
        return BeneficiaryResponse.from(beneficiaries.save(b));
    }

    @Transactional
    public void remove(User owner, Long id) {
        Beneficiary b = beneficiaries.findById(id)
                .orElseThrow(() -> ApiException.notFound("Beneficiary not found"));
        if (!b.getOwner().getId().equals(owner.getId())) {
            throw ApiException.forbidden("You don't own this beneficiary");
        }
        beneficiaries.delete(b);
    }
}
