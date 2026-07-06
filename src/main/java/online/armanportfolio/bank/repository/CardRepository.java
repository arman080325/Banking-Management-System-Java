package online.armanportfolio.bank.repository;

import online.armanportfolio.bank.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByAccountId(Long accountId);
    Optional<Card> findByAccountAccountNumber(Long accountNumber);
}
