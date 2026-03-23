package ci.jinx.qr_code.auth;

import ci.jinx.qr_code.auth.dto.AuthResponse;
import ci.jinx.qr_code.auth.dto.LoginRequest;
import ci.jinx.qr_code.auth.dto.RegisterRequest;
import ci.jinx.qr_code.config.JwtService;
import ci.jinx.qr_code.models.User;
import ci.jinx.qr_code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
// Lombok : génère automatiquement un objet "log" qu'on peut utiliser partout
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        log.info("Tentative d'inscription pour l'email : {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Inscription échouée — email déjà utilisé : {}", request.getEmail());
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("Utilisateur créé avec succès : {}", user.getEmail());

        var token = jwtService.generateToken(user);
        log.info("Token JWT généré pour : {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'email : {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.error("Échec de l'authentification pour : {} — Raison : {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé en base : {}", request.getEmail());
                    return new RuntimeException("Utilisateur non trouvé");
                });

        var token = jwtService.generateToken(user);
        log.info("Connexion réussie pour : {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}