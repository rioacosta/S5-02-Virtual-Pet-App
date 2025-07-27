package S502VirtualPetApp.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequestDTO {

    @Size(min = 3, max = 30, message = "El nombre debe tener entre 3 y 30 caracteres")
    private String username;

    @Email(message = "El formato del correo no es válido")
    private String email;

}
