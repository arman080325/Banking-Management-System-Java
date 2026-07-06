package online.armanportfolio.bank.service;

import online.armanportfolio.bank.dto.SupportTicketRequest;
import online.armanportfolio.bank.dto.SupportTicketResponse;
import online.armanportfolio.bank.model.SupportTicket;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupportService {

    private final SupportTicketRepository tickets;

    public SupportService(SupportTicketRepository tickets) {
        this.tickets = tickets;
    }

    @Transactional
    public SupportTicketResponse raise(User owner, SupportTicketRequest req) {
        SupportTicket t = new SupportTicket();
        t.setOwner(owner);
        t.setSubject(req.subject());
        t.setMessage(req.message());
        return SupportTicketResponse.from(tickets.save(t));
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> mine(User owner) {
        return tickets.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream().map(SupportTicketResponse::from).toList();
    }
}
