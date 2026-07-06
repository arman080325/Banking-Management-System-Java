package online.armanportfolio.bank.controller;

import online.armanportfolio.bank.dto.CardResponse;
import online.armanportfolio.bank.dto.CardUpdateRequest;
import online.armanportfolio.bank.service.CardService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final UserService userService;

    public CardController(CardService cardService, UserService userService) {
        this.cardService = cardService;
        this.userService = userService;
    }

    @GetMapping
    public List<CardResponse> mine() {
        return cardService.mine(userService.currentUser());
    }

    @PatchMapping("/{accountNumber}")
    public CardResponse update(@PathVariable Long accountNumber, @RequestBody CardUpdateRequest req) {
        return cardService.update(userService.currentUser(), accountNumber, req);
    }

    @PostMapping("/{accountNumber}/request-replacement")
    public CardResponse requestReplacement(@PathVariable Long accountNumber) {
        return cardService.requestReplacement(userService.currentUser(), accountNumber);
    }
}
