package online.armanportfolio.bank.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI bankingOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Banking System API")
                .description("Session-authenticated banking service — accounts, transfers, and a double-entry ledger.")
                .version("1.0.0")
                .contact(new Contact().name("Arman Ahemad Khan").url("https://arman-portfolio.online"))
                .license(new License().name("MIT")));
    }
}
