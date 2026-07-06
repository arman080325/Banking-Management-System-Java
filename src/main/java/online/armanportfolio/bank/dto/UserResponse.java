package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.User;

public record UserResponse(Long id, String fullName, String email) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail());
    }
}
