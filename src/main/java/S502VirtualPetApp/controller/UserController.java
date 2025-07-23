package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.dto.registerAndLogin.RegisterUserRequestDTO;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.BuddyService;
import S502VirtualPetApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final BuddyService buddyService;
    private final UserService userService;

    @GetMapping("/me")
    @Cacheable(value = "currentUser", key = "#user.username")
    @Operation(summary = "Getting an authenticated user")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        logger.info("Getting actual user: {}", user.getUsername());
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PostMapping("/register")
    @CacheEvict(value = "currentUser", key = "#currentUser.username")
    @Operation(summary = "User self-registration")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterUserRequestDTO request) {
        logger.info("Registering new user: {}", request.getUsername());
        UserDTO dto = new UserDTO();
        dto.setUsername(request.getUsername());
        dto.setEmail(request.getEmail());
        dto.setPassword(request.getPassword());
        dto.setRoles(Set.of(Role.USER));

        User created = userService.registerNewUser(dto);
        return ResponseEntity.ok(UserDTO.fromEntity(created));
    }

    @PutMapping("/update")
    @CacheEvict(value = "currentUser", key = "#currentUser.username")
    @Operation(summary = "Update data for authenticated user")
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserDTO userDTO
    ) {
        logger.info("Updating profile for: {}", currentUser.getUsername());
        User updated = userService.updateUser(currentUser.getUsername(), userDTO);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @GetMapping("/buddys")
    @Cacheable(value = "userBuddys", key = "#user.id")
    @Operation(summary = "Show user buddy´s")
    public List<BuddyDTO> getMyBuddys(@AuthenticationPrincipal User user) {
        return buddyService.getBuddysByOwner(user);
    }

    @PatchMapping("/change-password")
    @CacheEvict(value = "currentUser", key = "#user.username")
    @Operation(summary = "Change password for authenticated user")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        logger.info("Changing password for: {}", user.getUsername());
        userService.changePassword(user.getUsername(), oldPassword, newPassword);
        return ResponseEntity.noContent().build();
    }
}
