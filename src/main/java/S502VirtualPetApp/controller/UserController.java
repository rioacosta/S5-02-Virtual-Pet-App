package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.dto.registerAndLogin.RegisterUserRequestDTO;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.PetService;
import S502VirtualPetApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PetService petService;

    @GetMapping("/me")
    @Operation(summary = "Obtener el usuario autenticado")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @PostMapping("/register")
    @Operation(summary = "Autoregistro de usuario")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterUserRequestDTO request) {
        UserDTO dto = new UserDTO();
        dto.setUsername(request.getUsername());
        dto.setEmail(request.getEmail());
        dto.setPassword(request.getPassword());
        dto.setRoles(Set.of(Role.USER)); // ← asigna rol USER por defecto

        User created = userService.registerNewUser(dto);
        return ResponseEntity.ok(UserDTO.fromEntity(created));
    }

    @PutMapping("/update")
    @Operation(summary = "Actualizar datos del usuario autenticado")
    public ResponseEntity<UserDTO> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserDTO userDTO
    ) {
        User updated = userService.updateUser(currentUser.getUsername(), userDTO);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @PatchMapping("/change-password")
    @Operation(summary = "Cambiar contraseña del usuario autenticado")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        userService.changePassword(user.getUsername(), oldPassword, newPassword);
        return ResponseEntity.noContent().build();
    }

    /*@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    @Operation(summary = "\uD83D\uDD35 Crear nuevo usuario (admin)", description = "")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        User created = userService.registerNewUser(userDTO);
        return ResponseEntity.ok(UserDTO.fromEntity(created));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "\uD83D\uDD35 Listar todos los usuarios (admin)", description = "")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users.stream().map(UserDTO::fromEntity).toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users-with-pets")
    @Operation(summary = "\uD83D\uDD35 Listar todos los usuarios con sus mascotas (admin)", description = "")
    public List<AdminUserWithPetsDTO> getAllUsersWithPets() {
        List<User> users = userService.findAll();
        List<AdminUserWithPetsDTO> result = new ArrayList<>();

        for (User user : users) {
            List<PetDTO> pets = petService.getPetsByOwner(user); // ⬅️ Usas tu método existente
            result.add(AdminUserWithPetsDTO.fromEntity(user, pets));
        }

        return result;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    @Operation(summary = "\uD83D\uDD34 Eliminar usuario por username (admin)", description = "")
    public ResponseEntity<Void> deleteByUsername(@PathVariable String username) {
        userService.deleteByUsername(username);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{username}/toggle-enabled")
    @Operation(summary = "\uD83D\uDD34 Bloquear o desbloquear usuario (admin)", description = "")
    public ResponseEntity<UserDTO> toggleEnabled(@PathVariable String username) {
        User updated = userService.toggleEnabled(username);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }*/
}
