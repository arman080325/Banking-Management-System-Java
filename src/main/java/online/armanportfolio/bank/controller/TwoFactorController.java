package online.armanportfolio.bank.controller;

import jakarta.validation.Valid;
import online.armanportfolio.bank.dto.*;
import online.armanportfolio.bank.exception.ApiException;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.UserRepository;
import online.armanportfolio.bank.security.TotpService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Two-factor authentication (TOTP) setup for an already-signed-in user.
 * The login-time verification step lives in AuthController, since at that
 * point the session isn't authenticated yet.
 */
@RestController
@RequestMapping("/api/2fa")
public class TwoFactorController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TotpService totpService;
    private final PasswordEncoder encoder;

    public TwoFactorController(UserService userService, UserRepository userRepository,
                                TotpService totpService, PasswordEncoder encoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.totpService = totpService;
        this.encoder = encoder;
    }

    @GetMapping("/status")
    public TotpStatusResponse status() {
        return new TotpStatusResponse(userService.currentUser().isTotpEnabled());
    }

    /** Generates a new secret and returns it for manual entry into an authenticator app. Not yet active. */
    @PostMapping("/setup")
    @Transactional
    public TotpSetupResponse setup() {
        User user = userService.currentUser();
        if (user.isTotpEnabled()) {
            throw ApiException.badRequest("2FA is already enabled — disable it first to reset the key");
        }
        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        userRepository.save(user);
        return new TotpSetupResponse(secret, totpService.otpAuthUri(secret, user.getEmail()));
    }

    /** Confirms the authenticator app is actually working before 2FA is switched on. */
    @PostMapping("/enable")
    @Transactional
    public void enable(@Valid @RequestBody TotpCodeRequest req) {
        User user = userService.currentUser();
        if (user.getTotpSecret() == null) {
            throw ApiException.badRequest("Start setup first");
        }
        if (!totpService.verify(user.getTotpSecret(), req.code())) {
            throw ApiException.totpInvalid("Incorrect code — check your authenticator app and try again");
        }
        user.setTotpEnabled(true);
        userRepository.save(user);
    }

    /** Requires both the account password and a valid code, so a hijacked session alone can't turn 2FA off. */
    @PostMapping("/disable")
    @Transactional
    public void disable(@Valid @RequestBody TotpDisableRequest req) {
        User user = userService.currentUser();
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.forbidden("Current password is incorrect");
        }
        if (!totpService.verify(user.getTotpSecret(), req.code())) {
            throw ApiException.totpInvalid("Incorrect code");
        }
        user.setTotpEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
    }
}
