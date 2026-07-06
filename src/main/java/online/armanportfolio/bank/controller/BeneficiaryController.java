package online.armanportfolio.bank.controller;

import jakarta.validation.Valid;
import online.armanportfolio.bank.dto.BeneficiaryRequest;
import online.armanportfolio.bank.dto.BeneficiaryResponse;
import online.armanportfolio.bank.service.BeneficiaryService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;
    private final UserService userService;

    public BeneficiaryController(BeneficiaryService beneficiaryService, UserService userService) {
        this.beneficiaryService = beneficiaryService;
        this.userService = userService;
    }

    @GetMapping
    public List<BeneficiaryResponse> mine() {
        return beneficiaryService.mine(userService.currentUser());
    }

    @PostMapping
    public ResponseEntity<BeneficiaryResponse> add(@Valid @RequestBody BeneficiaryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.add(userService.currentUser(), req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        beneficiaryService.remove(userService.currentUser(), id);
        return ResponseEntity.noContent().build();
    }
}
