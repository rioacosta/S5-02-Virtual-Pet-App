package S502VirtualPetApp.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Document(collection = "pets")
public class VirtualPet {
    @Id
    private String id;
    private String name;
    private String avatar;
    private int level;
    private int experience;
    private int happiness;
    private int meditationStreak;
    private int totalMeditationMinutes;
    private LocalDateTime lastHug;
    private LocalDateTime lastMeditation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String habitat; // 🌄 Imagen de fondo
    private List<String> rewards = new ArrayList<>(); // 🎁 Recompensas gráficas
    private List<String> avatarStages;
    private List<MeditationSession> sessionHistory = new ArrayList<>(); // 📜 Historial

    @DBRef
    private User owner;

    public VirtualPet() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.level = 1;
        this.experience = 0;
        this.happiness = 50;
        this.meditationStreak = 0;
        this.totalMeditationMinutes = 0;
        this.avatarStages = new ArrayList<>();
    }

    public VirtualPet(String name, String type, User owner) {
        this();
        this.name = name;
        this.avatar = type;
        this.owner = owner;
    }

    public void meditate(int minutes, String reward) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime previousMeditation = this.lastMeditation;

        this.totalMeditationMinutes += minutes;
        this.experience += minutes * 2;
        this.happiness = Math.min(100, this.happiness + (minutes / 2));
        this.lastMeditation = now;
        this.updatedAt = now;

        int requiredExp = level * 100;
        while (this.experience >= requiredExp) {
            this.level++;
            this.experience -= requiredExp;
            requiredExp = this.level * 100;
        }

        if (previousMeditation != null) {
            if (previousMeditation.toLocalDate().equals(yesterday.toLocalDate())) {
                this.meditationStreak++;
            } else if (!previousMeditation.toLocalDate().equals(now.toLocalDate())) {
                this.meditationStreak = 1;
            }
        } else {
            this.meditationStreak = 1;
        }

        if (reward != null) {
            this.rewards.add(reward);
        }

        this.sessionHistory.add(new MeditationSession(now, minutes, reward));
    }

    public void hug() {
        this.happiness = Math.min(100, this.happiness + 10);
        this.lastHug = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
