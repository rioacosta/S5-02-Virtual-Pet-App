package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.AuthUpdateResponseDTO;
import S502VirtualPetApp.dto.UserUpdateRequestDTO;
import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.dto.registerAndLogin.RegisterUserRequestDTO;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.security.JwtUtil;
import S502VirtualPetApp.service.BuddyService;
import S502VirtualPetApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final BuddyService buddyService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/me")
    @Operation(summary = "Getting an authenticated user")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        logger.info("Getting actual user: {}", user.getUsername());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        user = userService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PostMapping("/register")
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
    @Operation(summary = "User self-updating")
    public ResponseEntity<AuthUpdateResponseDTO> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UserUpdateRequestDTO request) {
        String username = userDetails.getUsername();
        logger.info("Updating profile for: {}", username);
        User updatedUser = userService.updateUser(username, request);


        String newToken = jwtUtil.generateToken(updatedUser);
        logger.info("Generated token: {}", newToken);

        return ResponseEntity.ok(new AuthUpdateResponseDTO(UserDTO.fromEntity(updatedUser), newToken));
    }


    @GetMapping("/buddys")
    @Operation(summary = "Show user buddy´s")
    public List<BuddyDTO> getMyBuddys(@AuthenticationPrincipal User user) {
        return buddyService.getBuddysByOwner(user);
    }

    @PatchMapping("/change-password")
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
