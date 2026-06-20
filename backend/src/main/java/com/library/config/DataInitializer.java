package com.library.config;

import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedLibrarian(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.existsByUsername("librarian")) {
                User user = new User();
                user.setUsername("librarian");
                user.setPassword(passwordEncoder.encode("librarian123"));
                user.setRole(Role.LIBRARIAN);
                userRepository.save(user);
            }
        };
    }
}
