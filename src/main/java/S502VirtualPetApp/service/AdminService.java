package S502VirtualPetApp.service;

import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.dto.admin.AdminUserWithBuddysDTO;
import S502VirtualPetApp.dto.registerAndLogin.RegisterUserRequestDTO;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualBuddyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VirtualBuddyRepository virtualBuddyRepository;
    private final BuddyService buddyService;

    @CacheEvict(value = "users", allEntries = true)
    public UserDTO createAdmin(RegisterUserRequestDTO request) {
        logger.info("Creating new admin: {}", request.getUsername());
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Set.of(Role.ADMIN, Role.USER)
        );
        return UserDTO.fromEntity(userRepository.save(user));
    }

    @Cacheable(value = "users")
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<AdminUserWithBuddysDTO> findUsersWithBuddys() {
        return userRepository.findAll().stream().map(user -> {
            List<BuddyDTO> buddy = buddyService.getBuddysByOwner(user);
            return AdminUserWithBuddysDTO.fromEntity(user, buddy);
        }).toList();
    }

    @CacheEvict(value = "users", allEntries = true)
    public User createUser(UserDTO dto) {
        logger.info("Creating user: {}", dto.getUsername());
        if (userRepository.existsByUsername(dto.getUsername())) {
            logger.warn("Conflict: username already exists");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            logger.warn("Conflict: email already exists");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User(
                dto.getUsername(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getRoles() == null ? Set.of(Role.USER) : dto.getRoles()
        );

        return userRepository.save(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "userByUsername", key = "#username"),
            @CacheEvict(value = "users", allEntries = true)
    })    public void deleteUserByUsername(String username) {
        logger.info("Deleting user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        virtualBuddyRepository.deleteAll(virtualBuddyRepository.findByOwner(user));
        userRepository.delete(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "userByUsername", key = "#username"),
            @CacheEvict(value = "users", allEntries = true)
    })
    public User toggleUserEnabled(String username) {
        logger.info("Toggling user status: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }
}
