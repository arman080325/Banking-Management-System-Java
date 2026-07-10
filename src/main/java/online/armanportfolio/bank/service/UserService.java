package online.armanportfolio.bank.service;

import online.armanportfolio.bank.dto.RegisterRequest;
import online.armanportfolio.bank.exception.ApiException;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public User register(RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.email())) {
            throw ApiException.conflict("An account already exists for that email");
        }
        if (req.dateOfBirth().isAfter(java.time.LocalDate.now().minusYears(18))) {
            throw ApiException.badRequest("You must be at least 18 years old to open an account");
        }
        User u = new User();
        u.setFullName(req.fullName());
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setPhone(req.phone());
        u.setDateOfBirth(req.dateOfBirth());
        u.setPanNumber(req.panNumber().toUpperCase());
        u.setAddress(req.address());
        return users.save(u);
    }

    /** The currently authenticated user, resolved from the security context. */
    @Transactional(readOnly = true)
    public User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        return users.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!encoder.matches(currentPassword, user.getPasswordHash())) {
            throw ApiException.forbidden("Current password is incorrect");
        }
        user.setPasswordHash(encoder.encode(newPassword));
        users.save(user);
    }

    /** Admin only — every registered user, newest first. */
    @Transactional(readOnly = true)
    public List<User> allUsers() {
        return users.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public User byId(Long id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
    }

    @Transactional(readOnly = true)
    public User byEmail(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
