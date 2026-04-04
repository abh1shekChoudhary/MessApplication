package org.messplacement.messsecond.Service;

import org.messplacement.messsecond.Dao.UserRepository;
import org.messplacement.messsecond.DTO.LoginRequest;
import org.messplacement.messsecond.DTO.LoginResponse;
import org.messplacement.messsecond.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Validates credentials and returns a signed JWT on success.
     * Returns 401 if username is not found or password does not match.
     */
    public ResponseEntity<LoginResponse> login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty() ||
                !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOpt.get();
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().name(),
                user.getRegNo()
        );
        return ResponseEntity.ok(new LoginResponse(token, user.getRole().name(), user.getRegNo()));
    }

    /**
     * Issues a short-lived (2hr) guest JWT without requiring a password.
     * The guest account is seeded by DataInitializer on startup.
     * Falls back gracefully if the guest account is missing.
     */
    public ResponseEntity<LoginResponse> issueGuestToken() {
        User guest = userRepository.findByUsername("guest").orElseGet(() -> {
            User fallback = new User();
            fallback.setUsername("guest");
            fallback.setRole(org.messplacement.messsecond.Entities.Role.ROLE_GUEST);
            return fallback;
        });
        String token = jwtUtil.generateToken(guest.getUsername(), guest.getRole().name(), null);
        return ResponseEntity.ok(new LoginResponse(token, guest.getRole().name(), null));
    }
}
