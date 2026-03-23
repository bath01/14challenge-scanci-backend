package ci.jinx.qr_code.auth;

import ci.jinx.qr_code.auth.dto.AuthResponse;
import ci.jinx.qr_code.auth.dto.LoginRequest;
import ci.jinx.qr_code.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentification", description = "Inscription et connexion")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Créer un compte")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Se connecter et obtenir un token JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}
