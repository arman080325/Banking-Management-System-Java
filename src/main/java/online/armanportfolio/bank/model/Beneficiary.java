package online.armanportfolio.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** A payee saved by a user for faster future transfers. */
@Entity
@Table(name = "beneficiaries", indexes = @Index(name = "idx_benef_owner", columnList = "owner_id"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficiary_seq")
    @SequenceGenerator(name = "beneficiary_seq", sequenceName = "beneficiary_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Long accountNumber;

    @Column(length = 60)
    private String nickname;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;
}
