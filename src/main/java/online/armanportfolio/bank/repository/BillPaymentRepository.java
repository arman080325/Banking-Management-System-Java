package online.armanportfolio.bank.repository;

import online.armanportfolio.bank.model.BillPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillPaymentRepository extends JpaRepository<BillPayment, Long> {
    Page<BillPayment> findByOwnerIdOrderByCreatedAtDesc(Long ownerId, Pageable pageable);
}
