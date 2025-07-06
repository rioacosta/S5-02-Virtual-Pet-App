package S5_02_Virtual_Pet_App.service;

import S5_02_Virtual_Pet_App.dto.PetDTO;
import S5_02_Virtual_Pet_App.model.User;
import S5_02_Virtual_Pet_App.model.VirtualPet;
import S5_02_Virtual_Pet_App.repository.UserRepository;
import S5_02_Virtual_Pet_App.repository.VirtualPetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import java.util.stream.Collectors;

@Service
public class PetService {

    @Autowired
    private VirtualPetRepository virtualPetRepository;

    @Autowired
    private UserRepository userRepository;

    public VirtualPet getPetEntityById(String petId) {
        return virtualPetRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Virtual Pet not found"));
    }

    public List<PetDTO> getAllPets() {
        return virtualPetRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PetDTO> getPetsByOwner(String ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        return virtualPetRepository.findByOwner(owner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PetDTO getPetById(String petId) {
        return virtualPetRepository.findById(petId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Virtual Pet not found"));
    }

    public PetDTO createPet(PetDTO dto) {
        User owner = userRepository.findById(dto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        VirtualPet pet = new VirtualPet(dto.getName(), dto.getAvatar(), owner);
        VirtualPet saved = virtualPetRepository.save(pet);
        return toDTO(saved);
    }

    public PetDTO updatePet(String id, PetDTO dto) {
        VirtualPet existing = virtualPetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Virtual Pet not found"));

        existing.setName(dto.getName());
        existing.setAvatar(dto.getAvatar());
        existing.setUpdatedAt(java.time.LocalDateTime.now());

        return toDTO(virtualPetRepository.save(existing));
    }

    public void deletePet(String id) {
        if (!virtualPetRepository.existsById(id)) {
            throw new RuntimeException("Virtual Pet not found");
        }
        virtualPetRepository.deleteById(id);
    }

    public PetDTO meditate(String petId, int minutes) {
        if (minutes != 5 && minutes != 10 && minutes != 15 && minutes != 20) {
            throw new IllegalArgumentException("Duración de meditación inválida. Solo se permiten 5, 10, 15 o 20 minutos.");
        }

        VirtualPet pet = virtualPetRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Virtual Pet not found"));

        String reward = assignReward(minutes);
        pet.meditate(minutes, reward);

        return toDTO(virtualPetRepository.save(pet));
    }

    public String assignReward(int minutes) {
        return switch (minutes) {
            case 5 -> "flower";
            case 10 -> "hat";
            case 15 -> "scarf";
            case 20 -> "glow";
            default -> null;
        };
    }


    public PetDTO toDTO(VirtualPet pet) {
        PetDTO dto = new PetDTO();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setAvatar(pet.getAvatar());
        dto.setLevel(pet.getLevel());
        dto.setExperience(pet.getExperience());
        dto.setHappiness(pet.getHappiness());
        dto.setHealth(pet.getHealth());
        dto.setMeditationStreak(pet.getMeditationStreak());
        dto.setTotalMeditationMinutes(pet.getTotalMeditationMinutes());
        dto.setLastHug(pet.getLastHug());
        dto.setLastMeditation(pet.getLastMeditation());
        dto.setCreatedAt(pet.getCreatedAt());
        dto.setUpdatedAt(pet.getUpdatedAt());
        dto.setOwnerId(pet.getOwner() != null ? pet.getOwner().getId() : null);
        return dto;
    }
}
