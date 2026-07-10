package online.armanportfolio.bank.controller;

import online.armanportfolio.bank.dto.AdminTicketResponse;
import online.armanportfolio.bank.dto.AdminUserResponse;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.service.AccountService;
import online.armanportfolio.bank.service.SupportService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only console: view every user and their accounts, place/lift fraud
 * holds, and triage support tickets across the whole bank.
 *
 * Guarded twice on purpose: the URL is matched to ADMIN in SecurityConfig,
 * and @PreAuthorize repeats the check here, so this stays locked down even
 * if the filter-chain matcher is ever edited incorrectly.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final SupportService supportService;

    public AdminController(UserService userService, AccountService accountService, SupportService supportService) {
        this.userService = userService;
        this.accountService = accountService;
        this.supportService = supportService;
    }

    @GetMapping("/users")
    public List<AdminUserResponse> users() {
        return userService.allUsers().stream()
                .map((User u) -> AdminUserResponse.from(u, accountService.myAccounts(u)))
                .toList();
    }

    @PostMapping("/accounts/{accountNumber}/freeze")
    public ResponseEntity<Void> freeze(@PathVariable Long accountNumber) {
        accountService.setFrozen(accountNumber, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts/{accountNumber}/unfreeze")
    public ResponseEntity<Void> unfreeze(@PathVariable Long accountNumber) {
        accountService.setFrozen(accountNumber, false);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tickets")
    public List<AdminTicketResponse> tickets() {
        return supportService.allTickets();
    }

    @PostMapping("/tickets/{id}/close")
    public ResponseEntity<Void> closeTicket(@PathVariable Long id) {
        supportService.close(id);
        return ResponseEntity.noContent().build();
    }
}
