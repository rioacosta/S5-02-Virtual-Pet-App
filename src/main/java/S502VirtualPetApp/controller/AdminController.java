package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.dto.admin.AdminUserWithBuddysDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.AdminService;
import S502VirtualPetApp.service.BuddyService;
import S502VirtualPetApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final AdminService adminService;
    private final BuddyService buddyService;

    @PostMapping("/users")
    @Operation(summary = "\uD83D\uDD35 Create new user (admin)", description = "")
    public UserDTO createUser(@RequestBody @Valid UserDTO dto) {
        logger.info("Creating new user: {}", dto.getUsername());
        return UserDTO.fromEntity(adminService.createUser(dto));
    }

    @GetMapping("/users")
    @Operation(summary = "\uD83D\uDD35 List all users (admin)", description = "")
    public List<UserDTO> getAllUsers() {
        logger.info("Getting all users (cached)");
        return adminService.findAllUsers().stream()
                .map(UserDTO::fromEntity)
                .toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users-with-buddys")
    @Operation(summary = "\uD83D\uDD35 List all users with their pets (admin)")
    public List<AdminUserWithBuddysDTO> getAllUsersWithBuddys() {
        logger.info("Getting all users with their buddys (cached)");
        List<User> users = userService.findAll();
        List<AdminUserWithBuddysDTO> result = new ArrayList<>();

        for (User user : users) {
            List<BuddyDTO> pets = buddyService.getBuddysByOwner(user); // ⬅️ Usas tu método existente
            result.add(AdminUserWithBuddysDTO.fromEntity(user, pets));
        }
        return result;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    @Operation(summary = "\uD83D\uDD34 Delete user by name (admin)", description = "")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username) {
        logger.warn("Deleting user: {}", username);
        userService.deleteByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{username}/toggle-enabled")
    @Operation(summary = "\uD83D\uDD34 Temporary block a user (admin)", description = "")
    public ResponseEntity<UserDTO> toggleEnabled(@PathVariable String username) {
        logger.info("Cambiando estado de usuario: {}", username);
        User updated = adminService.toggleUserEnabled(username);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

}
