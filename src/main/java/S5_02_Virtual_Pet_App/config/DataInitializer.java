package S5_02_Virtual_Pet_App.config;

import S5_02_Virtual_Pet_App.model.Role;
import S5_02_Virtual_Pet_App.model.User;
import S5_02_Virtual_Pet_App.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User(
                        "admin",
                        "admin@example.com",
                        passwordEncoder.encode("admin123"),
                        Set.of(Role.ADMIN, Role.USER)
                );
                userRepository.save(admin);
                System.out.println("✅ Usuario admin creado: admin / admin123");
            }
        };
    }
}
