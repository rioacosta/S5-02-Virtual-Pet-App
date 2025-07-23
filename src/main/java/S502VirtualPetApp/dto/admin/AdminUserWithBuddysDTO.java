package S502VirtualPetApp.dto.admin;

import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class AdminUserWithBuddysDTO {
    private String id;
    private String username;
    private String email;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private List<BuddyDTO> buddys;

    public static AdminUserWithBuddysDTO fromEntity(User user, List<BuddyDTO> buddyDTOS) {
        AdminUserWithBuddysDTO dto = new AdminUserWithBuddysDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        dto.setBuddys(buddyDTOS);
        dto.setRoles(user.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.toSet()));
        return dto;
    }
}
