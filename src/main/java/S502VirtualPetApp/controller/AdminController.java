package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.PetDTO;
import S502VirtualPetApp.dto.UserDTO;
import S502VirtualPetApp.dto.admin.AdminUserWithPetsDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.AdminService;
import S502VirtualPetApp.service.PetService;
import S502VirtualPetApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;
    private final AdminService adminService;
    private final PetService petService;

    @PostMapping("/users")
    @Operation(summary = "\uD83D\uDD35 Crear nuevo usuario (admin)", description = "")
    public UserDTO createUser(@RequestBody @Valid UserDTO dto) {
        return UserDTO.fromEntity(adminService.createUser(dto));
    }

    @GetMapping("/users")
    @Operation(summary = "\uD83D\uDD35 Listar todos los usuarios (admin)", description = "")
    public List<UserDTO> getAllUsers() {
        return adminService.findAllUsers().stream()
                .map(UserDTO::fromEntity)
                .toList();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users-with-pets")
    @Operation(summary = "\uD83D\uDD35 Listar todos los usuarios con sus mascotas (admin)")
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


    @PatchMapping("/users/{username}/toggle-enabled")
    @Operation(summary = "\uD83D\uDD34 Bloquear o desbloquear usuario (admin)", description = "")
    public ResponseEntity<UserDTO> toggleEnabled(@PathVariable String username) {
        User updated = adminService.toggleUserEnabled(username);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @GetMapping("/users-with-pets")
    public List<AdminUserWithPetsDTO> getUsersWithPets() {
        return adminService.findUsersWithPets();
    }

    @DeleteMapping("/users/{username}")
    @Operation(summary = "\uD83D\uDD34 Eliminar usuario por username (admin)", description = "")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        adminService.deleteUserByUsername(username);
        return ResponseEntity.noContent().build();
    }


}
