package online.armanportfolio.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Banking System.
 *
 * A Spring Boot rewrite of a console/JDBC banking app — now a session-authenticated
 * web service with a real double-entry ledger, BigDecimal money, and row-locked transfers.
 */
@SpringBootApplication
public class BankingApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }
}
