package org.messplacement.messsecond;

import org.messplacement.messsecond.DTO.LoginRequest;
import org.messplacement.messsecond.DTO.LoginResponse;
import org.messplacement.messsecond.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {
        "http://localhost:4200",
        "http://localhost",
        "https://mess-application-front-end-angular.vercel.app"
})
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /auth/login
     * Body: { "username": "...", "password": "..." }
     * Returns: { "token": "...", "role": "ROLE_ADMIN | ROLE_STUDENT | ROLE_GUEST", "regNo": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * GET /auth/guest-token
     * Issues a 2-hour read-only JWT for the guest/recruiter demo account.
     * No credentials required — this is intentionally public.
     */
    @GetMapping("/guest-token")
    public ResponseEntity<LoginResponse> guestToken() {
        return authService.issueGuestToken();
    }
}
