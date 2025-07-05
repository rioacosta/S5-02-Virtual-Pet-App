package S5_02_Virtual_Pet_App.dto;

import lombok.Data;

import java.time.LocalDateTime;

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
    private String ownerId;
}
