package S5_02_Virtual_Pet_App.dto.petActions;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HugRequestDTO {
    @NotBlank(message = "El ID del usuario que envía el abrazo es obligatorio")
    private String senderId;

}
