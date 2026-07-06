package online.armanportfolio.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

/** A virtual debit card issued against an account, one-to-one. */
@Entity
@Table(name = "cards", indexes = @Index(name = "idx_card_account", columnList = "account_id", unique = true))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "card_seq")
    @SequenceGenerator(name = "card_seq", sequenceName = "card_seq", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false, length = 20)
    private String network = "VISA";

    @Column(nullable = false)
    private int expiryMonth;

    @Column(nullable = false)
    private int expiryYear;

    @Column(nullable = false)
    private boolean frozen = false;

    @Column(nullable = false)
    private boolean contactlessEnabled = true;

    @Column(nullable = false)
    private boolean onlineEnabled = true;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyLimit = new BigDecimal("25000.00");

    private Instant replacementRequestedAt;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;
}
