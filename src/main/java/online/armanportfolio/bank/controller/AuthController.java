package online.armanportfolio.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import online.armanportfolio.bank.dto.*;
import online.armanportfolio.bank.exception.ApiException;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.security.TotpService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** Session attribute holding the user id waiting on a TOTP code, and how many wrong codes they've tried. */
    private static final String SESSION_MFA_USER_ID = "MFA_PENDING_USER_ID";
    private static final String SESSION_MFA_ATTEMPTS = "MFA_ATTEMPTS";
    private static final int MAX_MFA_ATTEMPTS = 5;

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final TotpService totpService;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService, AuthenticationManager authManager, TotpService totpService) {
        this.userService = userService;
        this.authManager = authManager;
        this.totpService = totpService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(userService.register(req)));
    }

    /**
     * First factor: email + password. If the account has 2FA enabled, this does NOT
     * establish an authenticated session — it stashes the user id on the (unauthenticated)
     * HTTP session and asks the client to call /login/2fa with a code. Otherwise it
     * behaves exactly as before.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req,
                              HttpServletRequest request,
                              jakarta.servlet.http.HttpServletResponse response) {
        // authManager throws BadCredentialsException (-> 401 via GlobalExceptionHandler) on failure.
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        User user = userService.byEmail(req.email());

        if (user.isTotpEnabled()) {
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_MFA_USER_ID, user.getId());
            session.setAttribute(SESSION_MFA_ATTEMPTS, 0);
            return LoginResponse.mfaRequired(user.getFullName());
        }

        establishSession(user, request, response);
        return LoginResponse.authenticated(UserResponse.from(user));
    }

    /** Second factor: the 6-digit authenticator code, completing the login started above. */
    @PostMapping("/login/2fa")
    public LoginResponse verifyLoginCode(@Valid @RequestBody TotpCodeRequest req,
                                          HttpServletRequest request,
                                          jakarta.servlet.http.HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        Long userId = session == null ? null : (Long) session.getAttribute(SESSION_MFA_USER_ID);
        if (userId == null) {
            throw ApiException.forbidden("No sign-in in progress -- please sign in again");
        }

        User user = userService.byId(userId);
        if (!totpService.verify(user.getTotpSecret(), req.code())) {
            int attempts = 1 + (int) session.getAttribute(SESSION_MFA_ATTEMPTS);
            if (attempts >= MAX_MFA_ATTEMPTS) {
                session.invalidate();
                throw ApiException.forbidden("Too many incorrect codes -- please sign in again");
            }
            session.setAttribute(SESSION_MFA_ATTEMPTS, attempts);
            throw ApiException.totpInvalid("Incorrect code -- " + (MAX_MFA_ATTEMPTS - attempts) + " attempts left");
        }

        session.removeAttribute(SESSION_MFA_USER_ID);
        session.removeAttribute(SESSION_MFA_ATTEMPTS);
        establishSession(user, request, response);
        return LoginResponse.authenticated(UserResponse.from(user));
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(userService.currentUser());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(userService.currentUser(), req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }

    private void establishSession(User user, HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
    }
}
