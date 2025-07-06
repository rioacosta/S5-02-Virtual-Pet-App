package S5_02_Virtual_Pet_App.dto.registerAndLogin;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String username;
    private String email;
}
