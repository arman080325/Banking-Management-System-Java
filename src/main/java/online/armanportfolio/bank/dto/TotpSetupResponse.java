package online.armanportfolio.bank.dto;

public record TotpSetupResponse(String secret, String otpAuthUri) {}
