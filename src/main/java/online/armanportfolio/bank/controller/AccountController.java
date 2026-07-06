package online.armanportfolio.bank.controller;

import jakarta.validation.Valid;
import online.armanportfolio.bank.dto.*;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.service.AccountService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    public AccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    private User me() { return userService.currentUser(); }

    @GetMapping
    public List<AccountResponse> myAccounts() {
        return accountService.myAccounts(me());
    }

    @GetMapping("/{number}")
    public AccountResponse one(@PathVariable Long number) {
        return accountService.getOwnedAccount(me(), number);
    }

    @GetMapping("/{number}/history")
    public PagedResponse<LedgerEntryResponse> history(
            @PathVariable Long number,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return accountService.history(me(), number, page, size);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.open(me(), req));
    }

    @PostMapping("/credit")
    public AccountResponse credit(@Valid @RequestBody MoneyRequest req) {
        return accountService.credit(me(), req);
    }

    @PostMapping("/debit")
    public AccountResponse debit(@Valid @RequestBody MoneyRequest req) {
        return accountService.debit(me(), req);
    }

    @PostMapping("/transfer")
    public AccountResponse transfer(@Valid @RequestBody TransferRequest req) {
        return accountService.transfer(me(), req);
    }

    /** Full (unpaginated) statement export — not limited to whatever page the UI has loaded. */
    @GetMapping("/{number}/statement.csv")
    public ResponseEntity<byte[]> statementCsv(@PathVariable Long number) {
        List<LedgerEntryResponse> rows = accountService.fullHistory(me(), number);
        StringBuilder sb = new StringBuilder("Description,Reference,Date,Type,Amount,Balance After\n");
        for (LedgerEntryResponse e : rows) {
            sb.append(csvField(e.description())).append(',')
              .append(csvField(e.reference())).append(',')
              .append(csvField(e.createdAt() == null ? "" : e.createdAt().toString())).append(',')
              .append(csvField(e.type())).append(',')
              .append(csvField(e.amount())).append(',')
              .append(csvField(e.balanceAfter())).append('\n');
        }
        byte[] body = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=\"statement-" + number + ".csv\"")
                .body(body);
    }

    private static String csvField(Object v) {
        String s = String.valueOf(v == null ? "" : v);
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
