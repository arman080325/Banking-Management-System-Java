package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.User;

import java.time.Instant;
import java.util.List;

public record AdminUserResponse(
        Long id, String fullName, String email, String role, Instant createdAt,
        List<AccountResponse> accounts
) {
    public static AdminUserResponse from(User u, List<AccountResponse> accounts) {
        return new AdminUserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(),
                u.getCreatedAt(), accounts);
    }
}
