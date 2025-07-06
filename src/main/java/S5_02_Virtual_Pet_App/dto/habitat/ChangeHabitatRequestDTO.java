package S5_02_Virtual_Pet_App.dto.habitat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeHabitatRequestDTO {
    @NotBlank(message = "El hábitat no puede estar vacío")
    private String habitat;
}
