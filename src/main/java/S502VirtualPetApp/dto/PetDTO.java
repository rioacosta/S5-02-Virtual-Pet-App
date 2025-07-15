package S502VirtualPetApp.dto;

import S502VirtualPetApp.model.MeditationSession;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PetDTO {
    private String id;
    private String name;
    private String avatar;
    private int level;
    private int experience;
    private int happiness;
    private int health;
    private int meditationStreak;
    private int totalMeditationMinutes;
    private LocalDateTime lastHug;
    private LocalDateTime lastMeditation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String habitat;
    private List<String> rewards;
    private List<MeditationSession> sessionHistory;

    private String ownerId;
}
