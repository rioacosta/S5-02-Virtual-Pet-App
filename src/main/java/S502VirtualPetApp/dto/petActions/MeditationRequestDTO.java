package S502VirtualPetApp.dto.petActions;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MeditationRequestDTO {
    @Min(value = 1, message = "Debe meditar al menos 1 minuto")
    @Max(value = 120, message = "No se puede meditar más de 2 horas")
    private int minutes;

}
