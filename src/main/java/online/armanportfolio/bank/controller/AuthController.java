package online.armanportfolio.bank.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import online.armanportfolio.bank.dto.*;
import online.armanportfolio.bank.model.User;
import online.armanportfolio.bank.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authManager;
    private final SecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService, AuthenticationManager authManager) {
        this.userService = userService;
        this.authManager = authManager;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(userService.register(req)));
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest req,
                              HttpServletRequest request,
                              jakarta.servlet.http.HttpServletResponse response) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response); // persist session

        return UserResponse.from(userService.currentUser());
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(userService.currentUser());
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userService.changePassword(userService.currentUser(), req.currentPassword(), req.newPassword());
        return ResponseEntity.noContent().build();
    }
}
