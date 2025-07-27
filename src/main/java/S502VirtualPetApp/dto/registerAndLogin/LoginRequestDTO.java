package S502VirtualPetApp.dto.registerAndLogin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "User name is mandatory")
    private String username;

    @NotBlank(message = "Password is mandatory")
    private String password;

}
