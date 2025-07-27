package S502VirtualPetApp.service;

import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.dto.admin.AdminUserWithBuddysDTO;
import S502VirtualPetApp.dto.registerAndLogin.RegisterUserRequestDTO;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualBuddyRepository;
import S502VirtualPetApp.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VirtualBuddyRepository virtualBuddyRepository;
    private final BuddyService buddyService;
    private final AuthUtil authUtil;

    public UserDTO createAdmin(RegisterUserRequestDTO request) {
        logger.info("Creating new admin: {}", request.getUsername());
        User admin = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Set.of(Role.ADMIN, Role.USER)
        );
        User savedAdmin = userRepository.save(admin);
        return UserDTO.fromEntity(userRepository.save(savedAdmin));
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public List<AdminUserWithBuddysDTO> findUsersWithBuddys() {
        return userRepository.findAll().stream()
                .map(user -> {
                    List<BuddyDTO> buddyList = buddyService.getBuddysByOwner(user);
                    return AdminUserWithBuddysDTO.fromEntity(user, buddyList);
                })
                .toList();
    }

    public AdminUserWithBuddysDTO findUserWithBuddysByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<BuddyDTO> buddies = buddyService.getBuddysByOwner(user);
        return AdminUserWithBuddysDTO.fromEntity(user, buddies);
    }

    public void safeDeleteUserByUsername(String targetUsername) {
        String currentUser = authUtil.getCurrentUser().getUsername();

        if (targetUsername.equals(currentUser)) {
            logger.warn("Intento bloqueado: el admin '{}' quiso eliminarse a sí mismo.", currentUser);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminarte a ti mismo.");
        }

        User user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        virtualBuddyRepository.deleteAll(virtualBuddyRepository.findByOwner(user));
        userRepository.delete(user);
    }

    public List<String> safeUpdateUserRoles(String targetUsername, List<String> newRoles) {
        String currentUser = authUtil.getCurrentUser().getUsername();

        if (targetUsername.equals(currentUser) && !newRoles.contains("ADMIN")) {
            logger.warn("El admin '{}' intentó quitarse su rol ADMIN.", currentUser);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes quitarte el rol ADMIN.");
        }

        User user = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Set<Role> updatedRoles = newRoles.stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());

        user.setRoles(updatedRoles);
        userRepository.save(user);

        return updatedRoles.stream().map(Enum::name).toList();
    }

    public User toggleUserEnabled(String username) {
        logger.info("Toggling user status: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }
}
