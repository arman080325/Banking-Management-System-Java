package online.armanportfolio.bank.dto;

/**
 * Response for POST /api/auth/login. If the account has 2FA enabled, credentials
 * alone don't establish a session — {@code mfaRequired} is true, {@code fullName}
 * is included only so the UI can greet the person by name, and {@code user} is
 * null until POST /api/auth/login/2fa succeeds with a valid code.
 */
public record LoginResponse(boolean mfaRequired, String fullName, UserResponse user) {
    public static LoginResponse mfaRequired(String fullName) {
        return new LoginResponse(true, fullName, null);
    }

    public static LoginResponse authenticated(UserResponse user) {
        return new LoginResponse(false, null, user);
    }
}
