package online.armanportfolio.bank.service;

import online.armanportfolio.bank.dto.RegisterRequest;
import online.armanportfolio.bank.exception.ApiException;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User u = new User();
        u.setFullName(req.fullName());
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(encoder.encode(req.password()));
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
}
