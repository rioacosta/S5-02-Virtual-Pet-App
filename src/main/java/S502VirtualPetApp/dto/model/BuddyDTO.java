package S502VirtualPetApp.dto.model;

import S502VirtualPetApp.model.MeditationSession;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BuddyDTO {
    private String id;
    private String name;
    private String avatar;
    private int level;
    private int experience;
    private int totalExperience;
    private int happiness;
    private int meditationStreak;
    private int totalMeditationMinutes;
    private LocalDateTime lastHug;
    private LocalDateTime lastMeditation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> rewards;
    private List<MeditationSession> sessionHistory;
    private List<String> avatarStages;
    private String ownerId;
}
