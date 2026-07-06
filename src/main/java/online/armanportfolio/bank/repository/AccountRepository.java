package online.armanportfolio.bank.repository;

import jakarta.persistence.LockModeType;
import online.armanportfolio.bank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(Long accountNumber);

    List<Account> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    boolean existsByAccountNumber(Long accountNumber);

    /**
     * Fetches an account with a row-level write lock. Two concurrent transfers
     * touching the same account are serialised here, so a balance can never be
     * updated from a stale read — the concurrency bug the original app had.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNumber = :number")
    Optional<Account> lockByAccountNumber(@Param("number") Long number);
}
