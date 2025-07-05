package S5_02_Virtual_Pet_App.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "virtual_pets")
public class VirtualPet {
    @Id
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

    @DBRef
    private User owner;


    public VirtualPet() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.level = 1;
        this.experience = 0;
        this.happiness = 50;
        this.health = 100;
        this.meditationStreak = 0;
        this.totalMeditationMinutes = 0;
    }

    public VirtualPet(String name, String type, User owner) {
        this();
        this.name = name;
        this.avatar = type;
        this.owner = owner;
    }

    // Business methods
    public void meditate(int minutes) {
        this.totalMeditationMinutes += minutes;
        this.experience += minutes * 2;
        this.happiness = Math.min(100, this.happiness + minutes);
        this.lastMeditation = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Level up logic
        int requiredExp = level * 100;
        if (experience >= requiredExp) {
            level++;
            experience -= requiredExp;
        }

        // Update streak
        if (lastMeditation != null &&
                lastMeditation.toLocalDate().equals(LocalDateTime.now().toLocalDate().minusDays(1))) {
            meditationStreak++;
        } else if (lastMeditation == null ||
                !lastMeditation.toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
            meditationStreak = 1;
        }
    }

    public void hug() {
        this.health = Math.min(100, this.health + 20);
        this.happiness = Math.min(100, this.happiness + 10);
        this.lastHug = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}