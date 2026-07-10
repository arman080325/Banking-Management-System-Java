package online.armanportfolio.bank.exception;

import org.springframework.http.HttpStatus;

/** Domain error carrying the HTTP status it should map to, plus an optional machine-readable code. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public ApiException(HttpStatus status, String message, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }

    public static ApiException notFound(String msg)   { return new ApiException(HttpStatus.NOT_FOUND, msg); }
    public static ApiException badRequest(String msg)  { return new ApiException(HttpStatus.BAD_REQUEST, msg); }
    public static ApiException forbidden(String msg)   { return new ApiException(HttpStatus.FORBIDDEN, msg); }
    public static ApiException conflict(String msg)    { return new ApiException(HttpStatus.CONFLICT, msg); }

    /** 428 Precondition Required — used to ask the client for a TOTP code before a high-value transfer proceeds. */
    public static ApiException totpRequired(String msg) {
        return new ApiException(HttpStatus.PRECONDITION_REQUIRED, msg, "TOTP_REQUIRED");
    }

    public static ApiException totpInvalid(String msg) {
        return new ApiException(HttpStatus.FORBIDDEN, msg, "TOTP_INVALID");
    }
}
