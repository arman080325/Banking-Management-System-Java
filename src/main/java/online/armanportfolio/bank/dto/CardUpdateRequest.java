package online.armanportfolio.bank.dto;

/** Any null field is left unchanged — allows partial toggle updates. */
public record CardUpdateRequest(Boolean frozen, Boolean contactlessEnabled, Boolean onlineEnabled) {}
