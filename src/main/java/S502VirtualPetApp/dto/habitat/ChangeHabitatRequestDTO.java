package S502VirtualPetApp.dto.habitat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeHabitatRequestDTO {
    @NotBlank(message = "El hábitat no puede estar vacío")
    private String habitat;
}
