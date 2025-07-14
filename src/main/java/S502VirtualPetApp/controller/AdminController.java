package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.UserDTO;
import S502VirtualPetApp.dto.admin.AdminUserWithPetsDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return adminService.findAllUsers().stream()
                .map(UserDTO::fromEntity)
                .toList();
    }

    @GetMapping("/users-with-pets")
    public List<AdminUserWithPetsDTO> getUsersWithPets() {
        return adminService.findUsersWithPets();
    }

    @PostMapping("/users")
    public UserDTO createUser(@RequestBody @Valid UserDTO dto) {
        return UserDTO.fromEntity(adminService.createUser(dto));
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        adminService.deleteUserByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{username}/toggle-enabled")
    public ResponseEntity<UserDTO> toggleEnabled(@PathVariable String username) {
        User updated = adminService.toggleUserEnabled(username);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }
}
