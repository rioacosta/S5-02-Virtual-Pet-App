package S502VirtualPetApp.service;

import S502VirtualPetApp.dto.PetDTO;
import S502VirtualPetApp.dto.petActions.CreateVirtualPetRequestDTO;
import S502VirtualPetApp.model.MeditationSession;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualPet;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualPetRepository;
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

    public List<PetDTO> getPetsByOwner(User owner) {
        return virtualPetRepository.findByOwner(owner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PetDTO getPetByIdOwned(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        return toDTO(pet);
    }

    public VirtualPet getPetEntityById(String petId, User owner) {
        return getAndValidateOwnership(petId, owner);
    }

    private VirtualPet getAndValidateOwnership(String petId, User owner) {
        VirtualPet pet = virtualPetRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Virtual Pet not found"));
        if (!pet.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Unauthorized access to pet");
        }
        return pet;
    }

    public PetDTO createPet(CreateVirtualPetRequestDTO request, User owner) {
        VirtualPet pet = new VirtualPet(request.getName(), request.getAvatar(), owner);
        VirtualPet saved = virtualPetRepository.save(pet);
        return toDTO(saved);
    }

    public PetDTO updatePet(String petId, PetDTO dto, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);

        pet.setName(dto.getName());
        pet.setAvatar(dto.getAvatar());
        pet.setUpdatedAt(java.time.LocalDateTime.now());

        return toDTO(virtualPetRepository.save(pet));
    }

    public void deletePet(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        virtualPetRepository.delete(pet);
    }

    public PetDTO meditate(String petId, int minutes, User owner) {
        if (minutes != 5 && minutes != 10 && minutes != 15 && minutes != 20) {
            throw new IllegalArgumentException("Duración inválida");
        }

        VirtualPet pet = getAndValidateOwnership(petId, owner);
        String reward = assignReward(minutes);
        pet.meditate(minutes, reward);

        return toDTO(virtualPetRepository.save(pet));
    }

    public PetDTO hug(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        pet.hug();
        return toDTO(virtualPetRepository.save(pet));
    }

    public PetDTO changeHabitat(String petId, String habitat, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        pet.setHabitat(habitat);
        pet.setUpdatedAt(java.time.LocalDateTime.now());
        return toDTO(virtualPetRepository.save(pet));
    }

    public List<String> getRewards(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        return pet.getRewards();
    }

    public List<MeditationSession> getMeditationHistory(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        return pet.getSessionHistory();
    }

    public PetDTO getFullStatus(String petId, User owner) {
        return toDTO(getAndValidateOwnership(petId, owner));
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
        dto.setHabitat(pet.getHabitat());
        dto.setRewards(pet.getRewards());
        dto.setSessionHistory(pet.getSessionHistory());
        dto.setOwnerId(pet.getOwner() != null ? pet.getOwner().getId() : null);
        return dto;
    }
}
