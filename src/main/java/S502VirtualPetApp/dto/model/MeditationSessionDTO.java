package S502VirtualPetApp.dto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeditationSessionDTO {
    private LocalDateTime date;
    private int minutes;
    private String reward;
    private String habitat;
}
