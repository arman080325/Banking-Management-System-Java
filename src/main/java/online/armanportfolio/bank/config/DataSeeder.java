package online.armanportfolio.bank.config;

import online.armanportfolio.bank.dto.*;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.repository.UserRepository;
import online.armanportfolio.bank.service.AccountService;
import online.armanportfolio.bank.service.BeneficiaryService;
import online.armanportfolio.bank.service.BillPayService;
import online.armanportfolio.bank.service.SupportService;
import online.armanportfolio.bank.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Seeds a demo user with two accounts, a saved beneficiary, a bill payment,
 * and a support ticket on first boot — so a fresh login shows every tab
 * with real data instead of several empty states. Credentials are shown
 * on the login screen.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository users, UserService userService, AccountService accountService,
                          BeneficiaryService beneficiaryService,
                          BillPayService billPayService, SupportService supportService) {
        return args -> {
            if (users.count() > 0) return;

            User demo = userService.register(new RegisterRequest(
                    "Demo User", "demo@bank.app", "demo12345"));

            AccountResponse primary = accountService.open(demo, new OpenAccountRequest(
                    "Demo User", new BigDecimal("50000.00"), "1234"));
            accountService.open(demo, new OpenAccountRequest(
                    "Demo Savings", new BigDecimal("12500.00"), "1234"));

            // A second user, so the demo has a real account to save as a beneficiary
            // and transfer to, instead of a self-referential placeholder.
            User other = userService.register(new RegisterRequest(
                    "Priya Shah", "priya@example.com", "password123"));
            AccountResponse otherAccount = accountService.open(other, new OpenAccountRequest(
                    "Priya Shah", new BigDecimal("8000.00"), "5678"));

            beneficiaryService.add(demo, new BeneficiaryRequest(
                    "Priya Shah", otherAccount.accountNumber(), "Priya"));

            billPayService.pay(demo, new BillPaymentRequest(
                    primary.accountNumber(), "Electricity", "BESCOM · 4471029",
                    new BigDecimal("1180.00"), "1234"));

            supportService.raise(demo, new SupportTicketRequest(
                    "Question about international transfers",
                    "Hi, does IndusTrust support transfers to overseas accounts? Thanks!"));
        };
    }
}

