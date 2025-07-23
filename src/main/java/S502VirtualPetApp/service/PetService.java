package S502VirtualPetApp.service;

import S502VirtualPetApp.dto.model.MeditationSessionDTO;
import S502VirtualPetApp.dto.model.PetDTO;
import S502VirtualPetApp.dto.petActions.CreateVirtualPetRequestDTO;
import S502VirtualPetApp.model.MeditationSession;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualPet;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualPetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import java.util.stream.Collectors;

@Service
public class PetService {
    private static final Logger logger = LoggerFactory.getLogger(PetService.class);

    @Autowired
    private VirtualPetRepository virtualPetRepository;

    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "petsByOwner", key = "#owner.getId()")
    public List<PetDTO> getPetsByOwner(User owner) {
        return virtualPetRepository.findByOwner(owner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "pet", key = "#petId")
    public PetDTO getPetByIdOwned(String petId, User owner) {
        logger.info("Finding buddy whit ID: {}", petId);
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        return toDTO(pet);
    }

    public VirtualPet getPetEntityById(String petId, User owner) {
        return getAndValidateOwnership(petId, owner);
    }

    private VirtualPet getAndValidateOwnership(String petId, User owner) {
        VirtualPet pet = virtualPetRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Virtual buddy not found"));
        if (!pet.getOwner().getId().equals(owner.getId())) {
            logger.warn("Unauthorized access attempt by user: {}", owner.getId());
            throw new RuntimeException("Unauthorized access to buddy");
        }
        return pet;
    }

    public PetDTO createPet(CreateVirtualPetRequestDTO request, User owner) {
        String avatar = request.getAvatar();
        if (avatar != null) {
            avatar = avatar.substring(avatar.lastIndexOf('/') + 1); // Extrae solo el nombre del archivo
        }
        logger.info("Creating buddy, type: {}", request.getAvatar());
        VirtualPet pet = new VirtualPet(request.getName(), avatar, owner);
        VirtualPet saved = virtualPetRepository.save(pet);
        return toDTO(saved);
    }

    @CacheEvict(value = "pet", key = "#petId")
    public PetDTO updatePet(String petId, PetDTO dto, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        logger.info("Updating buddy: {} with avatar {}", dto.getId(), dto.getAvatar());
        pet.setName(dto.getName());
        pet.setAvatar(dto.getAvatar());
        pet.setUpdatedAt(java.time.LocalDateTime.now());
        return toDTO(virtualPetRepository.save(pet));
    }

    public void deletePet(String petId, User owner) {
        logger.info("Deleting buddy, type: {}", petId);
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        virtualPetRepository.delete(pet);
    }

    public PetDTO meditate(String petId, int minutes, String habitat, User owner) {
        logger.info("Starting buddy meditation session: {}", petId, habitat);
        if (minutes < 1 || minutes > 120) {
            logger.warn("Session is too long to process, should be less tha 120 minutes: {}", minutes);
            throw new IllegalArgumentException("Session is too long");
        }

        VirtualPet pet = getAndValidateOwnership(petId, owner);
        String reward = assignReward(minutes);
        pet.meditate(minutes, reward, habitat);

        if (habitat != null && !habitat.isBlank()) {
            pet.setHabitat(habitat);
        }

        return toDTO(virtualPetRepository.save(pet));
    }

    public PetDTO hug(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        logger.info("Hugging buddy: {}", petId);
        pet.hug();
        return toDTO(virtualPetRepository.save(pet));
    }

    public List<String> getRewards(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        return pet.getRewards();
    }

    public List<MeditationSessionDTO> getMeditationHistoryDTO(String petId, User owner) {
        VirtualPet pet = getAndValidateOwnership(petId, owner);
        logger.info("Session history for: {}", petId);
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
                                session.getHabitat()
                        ))
                        .collect(Collectors.toList())
        );
        dto.setOwnerId(pet.getOwner() != null ? pet.getOwner().getId() : null);

        dto.setAvatarStages(buildAvatarStages(pet.getAvatar(), 4));
        return dto;
    }
}
