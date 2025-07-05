package S5_02_Virtual_Pet_App.dto;

import S5_02_Virtual_Pet_App.model.Role;
import S5_02_Virtual_Pet_App.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserDTO {

    private String id;

    @NotBlank
    private String username;

    @Email
    private String email;

    private String password; // solo para registro

    private Set<Role> roles;

    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    public static UserDTO fromEntity(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastLogin(user.getLastLogin());
        return dto;
    }
}
