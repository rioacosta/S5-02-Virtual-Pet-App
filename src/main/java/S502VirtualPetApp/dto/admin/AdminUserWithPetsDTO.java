package S502VirtualPetApp.dto.admin;

import S502VirtualPetApp.dto.PetDTO;
import S502VirtualPetApp.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class AdminUserWithPetsDTO {
    private String id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private List<PetDTO> pets;

    public static AdminUserWithPetsDTO fromEntity(User user, List<PetDTO> petDTOs) {
        AdminUserWithPetsDTO dto = new AdminUserWithPetsDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setPets(petDTOs);
        dto.setRoles(user.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet()));
        return dto;
    }
}
