package S5_02_Virtual_Pet_App.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MeditationRequestDTO {
    @Min(value = 1, message = "Debe meditar al menos 1 minuto")
    @Max(value = 120, message = "No se puede meditar más de 2 horas")
    private int minutes;

}
