package online.armanportfolio.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * An immutable ledger entry — one row per movement of money on an account.
 * This is the "complete transaction history" the original app claimed but never had.
 *
 * A transfer produces two entries (a DEBIT on the sender, a CREDIT on the receiver)
 * linked by the same {@code reference}, giving true double-entry bookkeeping.
 */
@Entity
@Table(name = "ledger_entries", indexes = {
        @Index(name = "idx_ledger_account", columnList = "account_id"),
        @Index(name = "idx_ledger_reference", columnList = "reference")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class LedgerEntry {

    public enum Type { CREDIT, DEBIT }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ledger_seq")
    @SequenceGenerator(name = "ledger_seq", sequenceName = "ledger_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Type type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** Running balance of the account immediately after this entry. */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(nullable = false, length = 120)
    private String description;

    /** Shared id linking the two legs of a transfer. */
    @Column(nullable = false, length = 40)
    private String reference;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;
}
