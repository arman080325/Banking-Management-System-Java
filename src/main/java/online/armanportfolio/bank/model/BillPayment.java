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
 * A bill payment / recharge, debited from a real account. Backed by a genuine
 * ledger entry on that account (see {@code reference}), not just a UI record.
 */
@Entity
@Table(name = "bill_payments", indexes = @Index(name = "idx_billpay_owner", columnList = "owner_id"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class BillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billpay_seq")
    @SequenceGenerator(name = "billpay_seq", sequenceName = "billpay_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, length = 60)
    private String category;

    @Column(nullable = false, length = 60)
    private String consumer;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** Reference of the underlying ledger debit — ties this record to the real transaction. */
    @Column(nullable = false, length = 40)
    private String reference;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;
}
