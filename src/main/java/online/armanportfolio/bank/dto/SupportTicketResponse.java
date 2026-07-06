package online.armanportfolio.bank.dto;

import online.armanportfolio.bank.model.SupportTicket;
import java.time.Instant;

public record SupportTicketResponse(
        Long id, String ticketNumber, String subject, String message, String status, Instant createdAt
) {
    public static SupportTicketResponse from(SupportTicket t) {
        return new SupportTicketResponse(
                t.getId(), "TKT-" + String.format("%06d", t.getId()),
                t.getSubject(), t.getMessage(), t.getStatus().name(), t.getCreatedAt());
    }
}
