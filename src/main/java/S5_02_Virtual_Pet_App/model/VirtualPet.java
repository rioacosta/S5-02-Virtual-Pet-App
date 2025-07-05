package S5_02_Virtual_Pet_App.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        // Guardamos el día anterior de meditación, si existía
        LocalDateTime previousMeditation = this.lastMeditation;

        // Actualizamos métricas base
        this.totalMeditationMinutes += minutes;
        this.experience += minutes * 2;
        this.happiness = Math.min(100, this.happiness + (minutes / 2)); // gana felicidad proporcional

        this.lastMeditation = now;
        this.updatedAt = now;

        // Nivel arriba si alcanza la experiencia requerida
        int requiredExp = level * 100;
        while (this.experience >= requiredExp) {
            this.level++;
            this.experience -= requiredExp;
            requiredExp = this.level * 100;
        }

        // Lógica de racha de meditación
        if (previousMeditation != null) {
            if (previousMeditation.toLocalDate().equals(yesterday.toLocalDate())) {
                this.meditationStreak++;
            } else if (!previousMeditation.toLocalDate().equals(now.toLocalDate())) {
                this.meditationStreak = 1;
            } // Si medita dos veces en el mismo día, no se reinicia
        } else {
            this.meditationStreak = 1; // Primera vez
        }
    }


    public void hug() {
        this.health = Math.min(100, this.health + 20);
        this.happiness = Math.min(100, this.happiness + 10);
        this.lastHug = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}