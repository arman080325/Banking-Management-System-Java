package online.armanportfolio.bank.service;

import online.armanportfolio.bank.dto.CardResponse;
import online.armanportfolio.bank.dto.CardUpdateRequest;
import online.armanportfolio.bank.exception.ApiException;
import online.armanportfolio.bank.model.Account;
import online.armanportfolio.bank.model.Card;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.AccountRepository;
import online.armanportfolio.bank.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Cards are issued lazily: the first time a card is requested for an account,
 * one is created and persisted, so every existing account transparently gets
 * a card without a migration step.
 */
@Service
public class CardService {

    private final CardRepository cards;
    private final AccountRepository accounts;

    public CardService(CardRepository cards, AccountRepository accounts) {
        this.cards = cards;
        this.accounts = accounts;
    }

    @Transactional
    public List<CardResponse> mine(User owner) {
        List<Account> owned = accounts.findByOwnerIdOrderByCreatedAtDesc(owner.getId());
        return owned.stream().map(this::getOrCreate).map(CardResponse::from).toList();
    }

    @Transactional
    public CardResponse update(User owner, Long accountNumber, CardUpdateRequest req) {
        Card card = getOrCreateOwned(owner, accountNumber);
        if (req.frozen() != null) card.setFrozen(req.frozen());
        if (req.contactlessEnabled() != null) card.setContactlessEnabled(req.contactlessEnabled());
        if (req.onlineEnabled() != null) card.setOnlineEnabled(req.onlineEnabled());
        return CardResponse.from(card);
    }

    @Transactional
    public CardResponse requestReplacement(User owner, Long accountNumber) {
        Card card = getOrCreateOwned(owner, accountNumber);
        card.setReplacementRequestedAt(Instant.now());
        return CardResponse.from(card);
    }

    private Card getOrCreateOwned(User owner, Long accountNumber) {
        Account a = accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> ApiException.notFound("Account not found"));
        if (!a.getOwner().getId().equals(owner.getId())) {
            throw ApiException.forbidden("You don't own this account");
        }
        return getOrCreate(a);
    }

    private Card getOrCreate(Account a) {
        return cards.findByAccountId(a.getId()).orElseGet(() -> {
            Card c = new Card();
            c.setAccount(a);
            var expiry = Instant.now().atZone(ZoneOffset.UTC).plusYears(4);
            c.setExpiryMonth(expiry.getMonthValue());
            c.setExpiryYear(expiry.getYear());
            return cards.save(c);
        });
    }
}
