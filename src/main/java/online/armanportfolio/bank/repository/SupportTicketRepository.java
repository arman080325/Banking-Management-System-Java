package online.armanportfolio.bank.repository;

import online.armanportfolio.bank.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
