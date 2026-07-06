package online.armanportfolio.bank.controller;

import jakarta.validation.Valid;
import online.armanportfolio.bank.dto.SupportTicketRequest;
import online.armanportfolio.bank.dto.SupportTicketResponse;
import online.armanportfolio.bank.service.SupportService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support/tickets")
public class SupportController {

    private final SupportService supportService;
    private final UserService userService;

    public SupportController(SupportService supportService, UserService userService) {
        this.supportService = supportService;
        this.userService = userService;
    }

    @GetMapping
    public List<SupportTicketResponse> mine() {
        return supportService.mine(userService.currentUser());
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponse> raise(@Valid @RequestBody SupportTicketRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportService.raise(userService.currentUser(), req));
    }
}
