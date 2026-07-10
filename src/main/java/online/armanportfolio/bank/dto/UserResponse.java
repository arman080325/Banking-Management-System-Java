package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.User;

import java.time.LocalDate;

public record UserResponse(
        Long id, String fullName, String email, String role,
        String phone, LocalDate dateOfBirth, String panNumber, String address,
        boolean totpEnabled
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(),
                u.getPhone(), u.getDateOfBirth(), u.getPanNumber(), u.getAddress(), u.isTotpEnabled());
    }
}
