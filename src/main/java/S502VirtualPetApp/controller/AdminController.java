package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.UserUpdateRequestDTO;
import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.dto.admin.AdminUserWithBuddysDTO;
import S502VirtualPetApp.dto.registerAndLogin.RegisterUserRequestDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.AdminService;
import S502VirtualPetApp.service.BuddyService;
import S502VirtualPetApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final UserService userService;
    private final AdminService adminService;
    private final BuddyService buddyService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-admin")
    @Operation(summary = "\uD83D\uDD35 Create new admin", description = "")
    public UserDTO createAdmin (@RequestBody @Valid RegisterUserRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = auth.getName();
        User adminUser = userService.findByUsername(adminUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin user not found"));
        logger.info("Admin {} is creating a new admin: {}", adminUser.getUsername(), dto.getUsername());
        return adminService.createAdmin(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
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
    @Operation(summary = "🔵 List all users with their pets (admin)")
    public List<AdminUserWithBuddysDTO> getAllUsersWithBuddys() {
        logger.info("Getting all users with their buddys");
        return adminService.findUsersWithBuddys();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{username}")
    @Operation(summary = "Get user by username with their buddies (admin)")
    public ResponseEntity<AdminUserWithBuddysDTO> getUserWithBuddysByUsername(@PathVariable String username) {
        AdminUserWithBuddysDTO userWithBuddys = adminService.findUserWithBuddysByUsername(username);
        return ResponseEntity.ok(userWithBuddys);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    @Operation(summary = "\uD83D\uDD34 Delete user by name (admin)", description = "")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username) {
        logger.warn("Deleting user: {}", username);
        adminService.safeDeleteUserByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{username}/toggle-enabled")
    @Operation(summary = "\uD83D\uDD34 Temporary block a user (admin)", description = "")
    public ResponseEntity<UserDTO> toggleEnabled(@PathVariable String username) {
        logger.info("User temporary blocked: {}", username);
        User updated = adminService.toggleUserEnabled(username);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{username}/roles")
    @Operation(summary = "Modify user roles (admin)")
    public ResponseEntity<List<String>> updateUserRoles(@PathVariable String username,
                                                        @RequestBody List<String> newRoles) {
        logger.info("User roles updated: {}", username);
        List<String> updated = adminService.safeUpdateUserRoles(username, newRoles);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/users/{username}/update")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Update users data (admin)")
    public ResponseEntity<?> updateUserByAdmin(@PathVariable String username,
                                               @RequestBody UserUpdateRequestDTO dto) {
        Optional<User> targetUser = userService.findByUsername(username);
        if (targetUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        logger.info("User data updated: {}", username);
        userService.updateUser(targetUser.get().getUsername(), dto);
        return ResponseEntity.ok().build();
    }
}
