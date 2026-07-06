package online.armanportfolio.bank.repository;

import online.armanportfolio.bank.model.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    Page<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    long countByAccountId(Long accountId);
}
