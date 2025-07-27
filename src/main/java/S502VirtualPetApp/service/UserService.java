package S502VirtualPetApp.service;

import S502VirtualPetApp.dto.UserUpdateRequestDTO;
import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 🔒 Cargar usuario para autenticación
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // 🟢 Crear usuario normal
    public User registerNewUser(@Valid UserDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.error("Error user already exist: {}", request.getUsername());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.error("Error user already exist: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getRoles() == null ? Set.of(Role.USER) : request.getRoles()
        );
        logger.info("User registration successful: {} - {}", request.getUsername(), request.getEmail());
        return userRepository.save(user);
    }

    // 🕒 Actualizar último login
    public void updateLastLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
        logger.info("Updated last login for user: {}", username);

    }

    // 🔍 Obtener todos los usuarios
    public List<User> findAll() {
        logger.info("Getting all users");
        return userRepository.findAll();
    }

    // 🔍 Buscar por username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 🗑️ Eliminar por username
    public void deleteByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        userRepository.delete(user);
        logger.info("User deleted: {}", username);
    }

    // ✏️ Actualizar datos (nombre, email)
    public User updateUser(String username, UserUpdateRequestDTO request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank() &&
                !request.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("User email is already in use: {}", request.getEmail());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
            }
            user.setEmail(request.getEmail());
            logger.info("User email updated: {}", request.getEmail());
        }


        if (request.getUsername() != null && !request.getUsername().isBlank() &&
                !request.getUsername().equals(user.getUsername())) {

            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn("User name is already in use: {}", request.getUsername());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
            }
            user.setUsername(request.getUsername());
            logger.info("User name updated: {}", request.getUsername());
        }

        return userRepository.save(user);
    }

    // 🔒 Cambiar contraseña
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.error("Password mismatch for user: {}", username);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Password changed for user: {}", username);
    }

    // 🟡 Activar / desactivar cuenta
    public User toggleEnabled(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    // 📦 Convertir entidad a UserDTO
    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }

}
