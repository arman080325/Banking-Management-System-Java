package online.armanportfolio.bank.repository;

import online.armanportfolio.bank.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
