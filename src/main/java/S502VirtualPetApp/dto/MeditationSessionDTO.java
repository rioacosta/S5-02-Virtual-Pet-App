package S502VirtualPetApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MeditationSessionDTO {
    private LocalDateTime date;
    private int minutes;
    private String reward;
    private String habitat;
}
