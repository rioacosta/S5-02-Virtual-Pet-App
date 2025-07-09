package S502VirtualPetApp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class MeditationSession {
    private LocalDateTime date;
    private int duration;
    private String reward;
}
