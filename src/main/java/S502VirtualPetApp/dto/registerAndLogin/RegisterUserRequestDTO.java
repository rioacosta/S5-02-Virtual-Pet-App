package S502VirtualPetApp.dto.registerAndLogin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserRequestDTO {
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 20)
    private String username;

    @Email(message = "Debe proporcionar un email válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;

}
