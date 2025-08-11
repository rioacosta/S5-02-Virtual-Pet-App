package S502VirtualPetApp.dto.registerAndLogin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserRequestDTO {
    @NotBlank(message = "Its mandatory to have a name")
    @Size(min = 2, max = 20)
    private String username;

    @Email(message = "Must provide a valid email")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @NotBlank(message = "Password can't be blank")
    @Size(min = 6, message = "Password should have at least 6 characters")
    private String password;

}
