package online.armanportfolio.bank.service;

import online.armanportfolio.bank.dto.*;
import online.armanportfolio.bank.exception.ApiException;
import online.armanportfolio.bank.model.Account;
import online.armanportfolio.bank.model.LedgerEntry;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.AccountRepository;
import online.armanportfolio.bank.repository.LedgerEntryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledger;
    private final PasswordEncoder encoder;

    public AccountService(AccountRepository accounts, LedgerEntryRepository ledger, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.encoder = encoder;
    }

    /* ---------- queries ---------- */

    @Transactional(readOnly = true)
    public List<AccountResponse> myAccounts(User owner) {
        return accounts.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream().map(AccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse getOwnedAccount(User owner, Long accountNumber) {
        return AccountResponse.from(requireOwned(owner, accountNumber));
    }

    @Transactional(readOnly = true)
    public PagedResponse<LedgerEntryResponse> history(User owner, Long accountNumber, int page, int size) {
        Account acct = requireOwned(owner, accountNumber);
        Page<LedgerEntry> p = ledger.findByAccountIdOrderByCreatedAtDesc(
                acct.getId(), PageRequest.of(page, Math.min(size, 100)));
        return PagedResponse.of(p, LedgerEntryResponse::from);
    }

    /* ---------- commands ---------- */

    @Transactional
    public AccountResponse open(User owner, OpenAccountRequest req) {
        Account a = new Account();
        a.setOwner(owner);
        a.setHolderName(req.holderName());
        a.setBalance(req.openingBalance());
        a.setPinHash(encoder.encode(req.pin()));
        a.setAccountNumber(generateAccountNumber());
        Account saved = accounts.save(a);

        if (req.openingBalance().signum() > 0) {
            writeEntry(saved, LedgerEntry.Type.CREDIT, req.openingBalance(),
                    saved.getBalance(), "Opening deposit", newReference());
        }
        return AccountResponse.from(saved);
    }

    @Transactional
    public AccountResponse credit(User owner, MoneyRequest req) {
        Account a = lockOwned(owner, req.accountNumber());
        verifyPin(a, req.pin());
        a.setBalance(a.getBalance().add(req.amount()));
        writeEntry(a, LedgerEntry.Type.CREDIT, req.amount(), a.getBalance(), "Cash deposit", newReference());
        return AccountResponse.from(a);
    }

    @Transactional
    public AccountResponse debit(User owner, MoneyRequest req) {
        return debitInternal(owner, req.accountNumber(), req.amount(), req.pin(), "Cash withdrawal").account();
    }

    /** Result of an internal debit: the updated account plus the ledger reference it produced. */
    public record DebitResult(AccountResponse account, String reference) {}

    /**
     * Debits an owned account with a caller-supplied description, returning the ledger
     * reference so calling features (e.g. bill pay) can link their own records to the
     * real transaction instead of keeping a shadow copy.
     */
    @Transactional
    public DebitResult debitInternal(User owner, Long accountNumber, BigDecimal amount, String pin, String description) {
        Account a = lockOwned(owner, accountNumber);
        verifyPin(a, pin);
        if (a.getBalance().compareTo(amount) < 0) {
            throw ApiException.badRequest("Insufficient balance");
        }
        a.setBalance(a.getBalance().subtract(amount));
        String ref = newReference();
        writeEntry(a, LedgerEntry.Type.DEBIT, amount, a.getBalance(), description, ref);
        return new DebitResult(AccountResponse.from(a), ref);
    }

    /** Full, unpaged ledger history for statement export (CSV, etc). */
    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> fullHistory(User owner, Long accountNumber) {
        Account acct = requireOwned(owner, accountNumber);
        return ledger.findByAccountIdOrderByCreatedAtDesc(acct.getId())
                .stream().map(LedgerEntryResponse::from).toList();
    }

    /**
     * Atomic transfer. Both accounts are locked (in a deterministic order to avoid
     * deadlocks), the receiver is verified to exist, and two ledger legs are written
     * under the same reference — fixing every flaw in the original transfer.
     */
    @Transactional
    public AccountResponse transfer(User owner, TransferRequest req) {
        if (req.fromAccountNumber().equals(req.toAccountNumber())) {
            throw ApiException.badRequest("Cannot transfer to the same account");
        }

        // Lock in a stable order (lowest number first) so concurrent opposite
        // transfers can't deadlock each other.
        long lo = Math.min(req.fromAccountNumber(), req.toAccountNumber());
        long hi = Math.max(req.fromAccountNumber(), req.toAccountNumber());
        Account first = lock(lo);
        Account second = lock(hi);
        Account from = req.fromAccountNumber() == lo ? first : second;
        Account to   = req.toAccountNumber()   == lo ? first : second;

        if (!from.getOwner().getId().equals(owner.getId())) {
            throw ApiException.forbidden("You don't own the source account");
        }
        verifyPin(from, req.pin());
        if (from.getBalance().compareTo(req.amount()) < 0) {
            throw ApiException.badRequest("Insufficient balance");
        }

        String ref = newReference();
        from.setBalance(from.getBalance().subtract(req.amount()));
        to.setBalance(to.getBalance().add(req.amount()));
        writeEntry(from, LedgerEntry.Type.DEBIT, req.amount(), from.getBalance(),
                "Transfer to #" + to.getAccountNumber(), ref);
        writeEntry(to, LedgerEntry.Type.CREDIT, req.amount(), to.getBalance(),
                "Transfer from #" + from.getAccountNumber(), ref);
        return AccountResponse.from(from);
    }

    /* ---------- helpers ---------- */

    private Account requireOwned(User owner, Long number) {
        Account a = accounts.findByAccountNumber(number)
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        if (!a.getOwner().getId().equals(owner.getId())) {
            throw ApiException.forbidden("You don't own this account");
        }
        return a;
    }

    private Account lockOwned(User owner, Long number) {
        Account a = lock(number);
        if (!a.getOwner().getId().equals(owner.getId())) {
            throw ApiException.forbidden("You don't own this account");
        }
        return a;
    }

    private Account lock(Long number) {
        return accounts.lockByAccountNumber(number)
                .orElseThrow(() -> ApiException.notFound("Account #" + number + " not found"));
    }

    private void verifyPin(Account a, String pin) {
        if (!encoder.matches(pin, a.getPinHash())) {
            throw ApiException.forbidden("Invalid security PIN");
        }
    }

    private void writeEntry(Account a, LedgerEntry.Type type, BigDecimal amount,
                            BigDecimal balanceAfter, String desc, String ref) {
        LedgerEntry e = new LedgerEntry();
        e.setAccount(a);
        e.setType(type);
        e.setAmount(amount);
        e.setBalanceAfter(balanceAfter);
        e.setDescription(desc);
        e.setReference(ref);
        ledger.save(e);
    }

    private String newReference() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Long generateAccountNumber() {
        long n;
        do {
            n = 10_000_000L + (long) (RANDOM.nextDouble() * 89_999_999L);
        } while (accounts.existsByAccountNumber(n));
        return n;
    }
}
