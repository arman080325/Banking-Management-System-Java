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
 * A bank account.
 *
 * Improvements over the original console app:
 *  - {@code balance} is {@link BigDecimal}, not {@code double} (no floating-point money).
 *  - Security PIN is a BCrypt hash, not plaintext.
 *  - {@code @Version} enables optimistic locking so concurrent transfers can't
 *    silently corrupt a balance (the original had no locking at all).
 *  - Account numbers come from a dedicated sequence, not "max + 1".
 */
@Entity
@Table(name = "accounts",
        indexes = {
                @Index(name = "idx_acct_number", columnList = "accountNumber", unique = true),
                @Index(name = "idx_acct_owner", columnList = "owner_id")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_seq")
    @SequenceGenerator(name = "account_seq", sequenceName = "account_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long accountNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String holderName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /** BCrypt hash of the 4–6 digit security PIN. */
    @Column(nullable = false)
    private String pinHash;

    @Version
    private Long version;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;
}
