package S502VirtualPetApp.dto.buddyActions;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class MeditationRequestDTO {
    @Min(value = 1, message = "Meditation session should be at least 1 minute")
    @Max(value = 120, message = "Meditation session can't be more than 2 hours")
    private int minutes;
    private String habitat;
}
