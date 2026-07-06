package online.armanportfolio.bank.controller;

import jakarta.validation.Valid;
import online.armanportfolio.bank.dto.BillPaymentRequest;
import online.armanportfolio.bank.dto.BillPaymentResponse;
import online.armanportfolio.bank.dto.PagedResponse;
import online.armanportfolio.bank.service.BillPayService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billpay")
public class BillPayController {

    private final BillPayService billPayService;
    private final UserService userService;

    public BillPayController(BillPayService billPayService, UserService userService) {
        this.billPayService = billPayService;
        this.userService = userService;
    }

    @GetMapping("/history")
    public PagedResponse<BillPaymentResponse> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return billPayService.history(userService.currentUser(), page, size);
    }

    @PostMapping
    public ResponseEntity<BillPaymentResponse> pay(@Valid @RequestBody BillPaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billPayService.pay(userService.currentUser(), req));
    }
}
