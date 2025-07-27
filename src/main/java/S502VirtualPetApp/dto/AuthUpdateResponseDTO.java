package S502VirtualPetApp.dto;

import S502VirtualPetApp.dto.model.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthUpdateResponseDTO {
    private UserDTO user;
    private String token;
}
