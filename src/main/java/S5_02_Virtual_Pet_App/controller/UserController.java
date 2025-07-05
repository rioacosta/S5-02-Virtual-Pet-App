package S5_02_Virtual_Pet_App.controller;

import S5_02_Virtual_Pet_App.dto.UserDTO;
import S5_02_Virtual_Pet_App.model.User;
import S5_02_Virtual_Pet_App.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 🟢 Obtener el usuario autenticado
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    // 🔵 Crear nuevo usuario (admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        User created = userService.createUser(userDTO);
        return ResponseEntity.ok(UserDTO.fromEntity(created));
    }

    // 🔵 Listar todos los usuarios (admin)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users.stream().map(UserDTO::fromEntity).toList());
    }

    // 🔴 Eliminar usuario por username (admin)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username) {
        userService.deleteByUsername(username);
        return ResponseEntity.noContent().build();
    }

    // 🟠 Actualizar datos del usuario autenticado
    @PutMapping("/update")
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserDTO userDTO
    ) {
        User updated = userService.updateUser(currentUser.getUsername(), userDTO);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    // 🟠 Cambiar contraseña del usuario autenticado
    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        userService.changePassword(user.getUsername(), oldPassword, newPassword);
        return ResponseEntity.noContent().build();
    }

    // 🔴 Bloquear o desbloquear usuario (admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/toggle-enabled")
    public ResponseEntity<UserDTO> toggleEnabled(@PathVariable String username) {
        User updated = userService.toggleEnabled(username);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }
}
