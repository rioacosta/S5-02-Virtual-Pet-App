package S502VirtualPetApp.service;

import S502VirtualPetApp.dto.model.MeditationSessionDTO;
import S502VirtualPetApp.dto.model.PetDTO;
import S502VirtualPetApp.dto.petActions.CreateVirtualPetRequestDTO;
import S502VirtualPetApp.model.MeditationSession;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualPet;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualPetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
        String avatar = request.getAvatar();
        if (avatar != null) {
            avatar = avatar.substring(avatar.lastIndexOf('/') + 1); // Extrae solo el nombre del archivo
        }
        VirtualPet pet = new VirtualPet(request.getName(), avatar, owner);
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

    public PetDTO meditate(String petId, int minutes, String habitat, User owner) {
        if (minutes < 1 || minutes > 120) {
            throw new IllegalArgumentException("Duración inválida");
        }

        VirtualPet pet = getAndValidateOwnership(petId, owner);
        String reward = assignReward(minutes);
        pet.meditate(minutes, reward, habitat);

        if (habitat != null && !habitat.isBlank()) {
            pet.setHabitat(habitat); // << AÑADIR ESTO
        }

        return toDTO(virtualPetRepository.save(pet));
    }

    public PetDTO hug(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        pet.hug();
        return toDTO(virtualPetRepository.save(pet));
    }

    /*public PetDTO changeHabitat(String petId, String habitat, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        pet.setHabitat(habitat);
        pet.setUpdatedAt(java.time.LocalDateTime.now());
        return toDTO(virtualPetRepository.save(pet));
    }*/

    public List<String> getRewards(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        return pet.getRewards();
    }

    public List<MeditationSessionDTO> getMeditationHistoryDTO(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);

        return pet.getSessionHistory().stream()
                .map(session -> new MeditationSessionDTO(
                        session.getDate(),
                        session.getDuration(),
                        session.getReward(),
                        session.getHabitat()
                ))
                .collect(Collectors.toList());
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

    public PetDTO addReward(String petId, String rewardName, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);

        if (!pet.getRewards().contains(rewardName)) {
            pet.getRewards().add(rewardName);
            pet.setUpdatedAt(java.time.LocalDateTime.now());
            virtualPetRepository.save(pet);
        }

        return toDTO(pet);
    }

    private void decayHappinessIfInactive(VirtualPet pet) {
        LocalDateTime lastMeditation = pet.getLastMeditation();
        LocalDateTime lastHug = pet.getLastHug();

        LocalDateTime latestInteraction = null;

        if (lastMeditation != null && lastHug != null) {
            latestInteraction = lastMeditation.isAfter(lastHug) ? lastMeditation : lastHug;
        } else if (lastMeditation != null) {
            latestInteraction = lastMeditation;
        } else if (lastHug != null) {
            latestInteraction = lastHug;
        }

        if (latestInteraction == null || latestInteraction.isBefore(LocalDateTime.now().minusDays(1))) {
            pet.setHappiness(0);
            pet.setUpdatedAt(LocalDateTime.now());
            virtualPetRepository.save(pet);
        }
    }
    private List<String> buildAvatarStages(String avatar, int count) {
        List<String> stages = new ArrayList<>();
        if (avatar != null && avatar.endsWith(".png")) {
            avatar = avatar.substring(0, avatar.length() - 4);
        }
        for (int i = 1; i <= count; i++) {
            stages.add("/assets/avatars/" + avatar + "_stage" + i + ".png");
        }
        return stages;
    }

    public PetDTO getFullStatus(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        decayHappinessIfInactive(pet);
        return toDTO(pet);
    }

    private PetDTO toDTO(VirtualPet pet) {
        PetDTO dto = new PetDTO();
        dto.setId(pet.getId());
        dto.setName(pet.getName());
        dto.setAvatar(pet.getAvatar());
        dto.setLevel(pet.getLevel());
        dto.setExperience(pet.getExperience());
        dto.setHappiness(pet.getHappiness());
        dto.setMeditationStreak(pet.getMeditationStreak());
        dto.setTotalMeditationMinutes(pet.getTotalMeditationMinutes());
        dto.setLastHug(pet.getLastHug());
        dto.setLastMeditation(pet.getLastMeditation());
        dto.setCreatedAt(pet.getCreatedAt());
        dto.setUpdatedAt(pet.getUpdatedAt());
        //dto.setHabitat(pet.getHabitat());
        dto.setRewards(pet.getRewards());
        dto.setSessionHistory(
                pet.getSessionHistory().stream()
                        .map(session -> new MeditationSession(
                                session.getDate(),
                                session.getDuration(),
                                session.getReward(),
                                session.getHabitat() // si aplica el mismo habitat para todas
                        ))
                        .collect(Collectors.toList())
        );
        dto.setOwnerId(pet.getOwner() != null ? pet.getOwner().getId() : null);

        dto.setAvatarStages(buildAvatarStages(pet.getAvatar(), 4));
        return dto;
    }
}
