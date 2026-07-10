package online.armanportfolio.bank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

/**
 * An authenticated user. Passwords are stored only as BCrypt hashes —
 * the original stored them in plaintext.
 */
@Entity
@Table(name = "users", indexes = @Index(name = "idx_user_email", columnList = "email", unique = true))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String fullName;

    /** BCrypt hash — never the raw password. */
    @Column(nullable = false)
    private String passwordHash;

    /** KYC fields collected at registration. Nullable so pre-existing rows stay valid. */
    @Column(length = 10)
    private String phone;

    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String panNumber;

    @Column(length = 200)
    private String address;

    /** USER by default; ADMIN unlocks the admin console and its endpoints. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10, columnDefinition = "varchar(10) default 'USER'")
    private Role role = Role.USER;

    /** Base32 TOTP seed. Set on /2fa/setup, only trusted once totpEnabled is true. */
    @Column(length = 64)
    private String totpSecret;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean totpEnabled = false;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    public enum Role { USER, ADMIN }
}
