package com.example.Stepway.Configuration;

import com.example.Stepway.Domain.Role;
import com.example.Stepway.Domain.User;
import com.example.Stepway.Repository.RoleRepository;
import com.example.Stepway.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;

/** Initializes a fresh demo database without resetting existing accounts. */
@Component
public class DemoBootstrap implements ApplicationRunner {
    private final RoleRepository roles;
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public DemoBootstrap(RoleRepository roles, UserRepository users, PasswordEncoder encoder,
                         @Value("${app.bootstrap.admin-email}") String email,
                         @Value("${app.bootstrap.admin-password}") String password) {
        this.roles = roles;
        this.users = users;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String name : new String[]{"ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT"}) {
            if (!roles.findByName(name).isPresent()) {
                Role role = new Role();
                role.setName(name);
                role.setStatus(true);
                roles.save(role);
            }
        }
        if (email.isEmpty() && password.isEmpty()) return;
        if (email.trim().isEmpty() || password.length() < 12) {
            throw new IllegalArgumentException("Set both DEMO_ADMIN_EMAIL and DEMO_ADMIN_PASSWORD (at least 12 characters)");
        }
        if (users.findByEmail(email) == null) {
            User admin = new User();
            admin.setFirstName("Demo");
            admin.setLastName("Admin");
            admin.setEmail(email);
            admin.setPassword(encoder.encode(password));
            admin.setRole(Collections.singleton(roles.findByName("ROLE_ADMIN").get()));
            users.save(admin);
        }
    }
}
