package online.armanportfolio.bank.service;

import online.armanportfolio.bank.dto.BillPaymentRequest;
import online.armanportfolio.bank.dto.BillPaymentResponse;
import online.armanportfolio.bank.dto.PagedResponse;
import online.armanportfolio.bank.exception.ApiException;
import online.armanportfolio.bank.model.Account;
import online.armanportfolio.bank.model.BillPayment;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.AccountRepository;
import online.armanportfolio.bank.repository.BillPaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillPayService {

    private final BillPaymentRepository billPayments;
    private final AccountRepository accounts;
    private final AccountService accountService;

    public BillPayService(BillPaymentRepository billPayments, AccountRepository accounts, AccountService accountService) {
        this.billPayments = billPayments;
        this.accounts = accounts;
        this.accountService = accountService;
    }

    @Transactional
    public BillPaymentResponse pay(User owner, BillPaymentRequest req) {
        var result = accountService.debitInternal(owner, req.accountNumber(), req.amount(), req.pin(),
                req.category() + " · " + req.consumer());

        Account acct = accounts.findByAccountNumber(req.accountNumber())
                .orElseThrow(() -> ApiException.notFound("Account not found"));

        BillPayment bp = new BillPayment();
        bp.setOwner(owner);
        bp.setAccount(acct);
        bp.setCategory(req.category());
        bp.setConsumer(req.consumer());
        bp.setAmount(req.amount());
        bp.setReference(result.reference());
        return BillPaymentResponse.from(billPayments.save(bp));
    }

    @Transactional(readOnly = true)
    public PagedResponse<BillPaymentResponse> history(User owner, int page, int size) {
        Page<BillPayment> p = billPayments.findByOwnerIdOrderByCreatedAtDesc(
                owner.getId(), PageRequest.of(page, Math.min(size, 50)));
        return PagedResponse.of(p, BillPaymentResponse::from);
    }
}
