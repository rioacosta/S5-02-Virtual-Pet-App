package S502VirtualPetApp.dto.buddyActions;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HugRequestDTO {
    @NotBlank(message = "El ID del usuario que envía el abrazo es obligatorio")
    private String senderId;

}
