package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.SupportTicket;

import java.time.Instant;

public record AdminTicketResponse(
        Long id, String ticketNumber, String subject, String message, String status, Instant createdAt,
        String ownerName, String ownerEmail
) {
    public static AdminTicketResponse from(SupportTicket t) {
        return new AdminTicketResponse(
                t.getId(), "TKT-" + String.format("%06d", t.getId()),
                t.getSubject(), t.getMessage(), t.getStatus().name(), t.getCreatedAt(),
                t.getOwner().getFullName(), t.getOwner().getEmail());
    }
}
