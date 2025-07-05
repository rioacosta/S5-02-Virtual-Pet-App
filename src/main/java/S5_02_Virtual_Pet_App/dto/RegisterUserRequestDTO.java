package S5_02_Virtual_Pet_App.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterUserRequestDTO {
    @NotBlank
    private String username;

    @Email
    private String email;

    @NotBlank
    private String password;
}
