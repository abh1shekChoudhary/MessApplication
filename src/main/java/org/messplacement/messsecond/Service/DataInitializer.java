package org.messplacement.messsecond.Service;

import org.messplacement.messsecond.Dao.UserRepository;
import org.messplacement.messsecond.Entities.Role;
import org.messplacement.messsecond.Entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default users on first startup if they do not already exist.
 *
 * Default accounts:
 *   admin   / AdminPassword123   — ROLE_ADMIN
 *   guest   / GuestDemo123       — ROLE_GUEST   (also reachable via /auth/guest-token)
 *   22BAI10072 / StudentPass123  — ROLE_STUDENT (demo student account)
 *
 * IMPORTANT: Change these passwords immediately after first deployment via your
 * admin user-management interface (to be built in a later sprint).
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedUser("admin",      "AdminPassword123",  Role.ROLE_ADMIN,   null);
        seedUser("guest",      "GuestDemo123",      Role.ROLE_GUEST,   null);
        seedUser("22BAI10072", "StudentPass123",     Role.ROLE_STUDENT, "22BAI10072");
    }

    private void seedUser(String username, String rawPassword, Role role, String regNo) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setRegNo(regNo);
            userRepository.save(user);
            System.out.printf("[DataInitializer] Seeded user '%s' with role %s%n", username, role);
        }
    }
}
