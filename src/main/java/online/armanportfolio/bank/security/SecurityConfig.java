package online.armanportfolio.bank.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;

/**
 * Session-based authentication. The API returns JSON 401/403 instead of redirecting
 * to a login page, so the vanilla-JS frontend can handle auth cleanly.
 *
 * Public: register, login, the static UI, health, and Swagger.
 * Everything under /api/** (except auth) requires an authenticated session.
 * /api/admin/** additionally requires the ADMIN role — enforced both at the
 * filter-chain level below and again at the method level via @PreAuthorize
 * on AdminController, so a misconfigured matcher can't silently open it up.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())            // token-less JSON API; SameSite session cookie
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/index.html", "/login.html", "/admin.html", "/favicon.ico",
                        "/css/**", "/js/**", "/assets/**",
                        "/api/auth/register", "/api/auth/login", "/api/auth/login/2fa",
                        "/actuator/health",
                        "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
                ).permitAll()
                // admin.html itself is a static file like any other page (the JS on it
                // immediately calls /api/admin/users and bounces non-admins away) —
                // the real boundary is here, on the data endpoints.
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .logout(l -> l.logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, a) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)))
            .headers(h -> h
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; script-src 'self'; " +
                    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; " +
                    "font-src 'self' https://fonts.gstatic.com; img-src 'self' data:; " +
                    "connect-src 'self'; base-uri 'self'; frame-ancestors 'none'; form-action 'self'"))
                .referrerPolicy(r -> r.policy(
                    org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)));

        return http.build();
    }
}
