package S502VirtualPetApp.service;

import S502VirtualPetApp.dto.model.MeditationSessionDTO;
import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.dto.buddyActions.CreateVirtualBuddyRequestDTO;
import S502VirtualPetApp.model.MeditationSession;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualBuddy;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualBuddyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class BuddyService {
    private static final Logger logger = LoggerFactory.getLogger(BuddyService.class);

    @Autowired
    private VirtualBuddyRepository virtualBuddyRepository;

    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "petsByOwner", key = "#owner.getId()")
    public List<BuddyDTO> getBuddysByOwner(User owner) {
        return virtualBuddyRepository.findByOwner(owner).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "pet", key = "#petId")
    public BuddyDTO getBuddyByIdOwned(String petId, User owner) {
        logger.info("Finding buddy whit ID: {}", petId);
        VirtualBuddy buddy = getAndValidateOwnership(petId, owner);
        decayHappinessIfInactive(buddy);
        return toDTO(buddy);
    }

    private VirtualBuddy getAndValidateOwnership(String petId, User owner) {
        VirtualBuddy pet = virtualBuddyRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Virtual buddy not found"));
        if (!pet.getOwner().getId().equals(owner.getId())) {
            logger.warn("Unauthorized access attempt by user: {}", owner.getId());
            throw new RuntimeException("Unauthorized access to buddy");
        }
        return pet;
    }

    @CacheEvict(value = "buddysByOwner", key = "#owner.id")
    public BuddyDTO createBuddy(CreateVirtualBuddyRequestDTO request, User owner) {
        String avatar = request.getAvatar();
        if (avatar != null) {
            avatar = avatar.substring(avatar.lastIndexOf('/') + 1); // Extrae solo el nombre del archivo
        }
        logger.info("Creating buddy, type: {}", request.getAvatar());
        VirtualBuddy buddy = new VirtualBuddy(request.getName(), avatar, owner);
        VirtualBuddy saved = virtualBuddyRepository.save(buddy);
        return toDTO(saved);
    }

    @CacheEvict(value = "buddy", key = "#buddyId")
    public BuddyDTO updateBuddy(String buddyId, BuddyDTO dto, User owner) {
        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);
        logger.info("Updating buddy: {} with avatar {}", dto.getId(), dto.getAvatar());
        buddy.setName(dto.getName());
        buddy.setAvatar(dto.getAvatar());
        buddy.setUpdatedAt(java.time.LocalDateTime.now());
        return toDTO(virtualBuddyRepository.save(buddy));
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "buddy", key = "#buddyId"),
                    @CacheEvict(value = "buddysByOwner", key = "#owner.id"),
                    @CacheEvict(value = "buddyStatus", key = "#buddyId")
            }    )
    public void deleteBuddy(String buddyId, User owner) {
        logger.info("Deleting buddy, type: {}", buddyId);
        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);
        virtualBuddyRepository.delete(buddy);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "buddy", key = "#buddyId"),
                    @CacheEvict(value = "buddysByOwner", key = "#owner.id"),
                    @CacheEvict(value = "buddyStatus", key = "#buddyId")
            }    )
    public BuddyDTO meditate(String buddyId, int minutes, String habitat, User owner) {
        logger.info("Starting buddy meditation session: {}", buddyId, habitat);
        if (minutes < 1 || minutes > 120) {
            logger.warn("Session is too long to process, should be less tha 120 minutes: {}", minutes);
            throw new IllegalArgumentException("Session is too long");
        }

        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);
        String reward = assignReward(minutes);
        buddy.meditate(minutes, reward, habitat);

        if (habitat != null && !habitat.isBlank()) {
            buddy.setHabitat(habitat);
        }

        return toDTO(virtualBuddyRepository.save(buddy));
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "buddy", key = "#buddyId"),
                    @CacheEvict(value = "buddyByOwner", key = "#owner.id"),
                    @CacheEvict(value = "buddyStatus", key = "#buddyId")
            }    )
    public BuddyDTO hug(String buddyId, User owner) {
        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);
        logger.info("Hugging buddy: {}", buddyId);
        buddy.hug();
        return toDTO(virtualBuddyRepository.save(buddy));
    }

    public List<String> getRewards(String buddyId, User owner) {
        VirtualBuddy pet = getAndValidateOwnership(buddyId, owner);
        return pet.getRewards();
    }

    public List<MeditationSessionDTO> getMeditationHistoryDTO(String buddyId, User owner) {
        VirtualBuddy pet = getAndValidateOwnership(buddyId, owner);
        logger.info("Session history for: {}", buddyId);
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

    public BuddyDTO addReward(String buddyId, String rewardName, User owner) {
        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);

        if (!buddy.getRewards().contains(rewardName)) {
            buddy.getRewards().add(rewardName);
            buddy.setUpdatedAt(java.time.LocalDateTime.now());
            virtualBuddyRepository.save(buddy);
        }

        return toDTO(buddy);
    }

    private void decayHappinessIfInactive(VirtualBuddy buddy) {
        LocalDateTime lastInteraction = Stream.of(buddy.getLastMeditation(), buddy.getLastHug())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (lastInteraction == null) {
            buddy.setHappiness(0);
            return;
        }

        long daysInactive = ChronoUnit.DAYS.between(lastInteraction, LocalDateTime.now());
        if (daysInactive > 0) {
            int decay = (int) (daysInactive * 5); // -5 puntos por día
            int newHappiness = Math.max(0, buddy.getHappiness() - decay);
            buddy.setHappiness(newHappiness);
            buddy.setUpdatedAt(LocalDateTime.now());
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

    @Cacheable(value = "buddyStatus", key = "#petId")
    public BuddyDTO getFullStatus(String buddyId, User owner) {
        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);
        decayHappinessIfInactive(buddy);
        return toDTO(buddy);
    }

    private BuddyDTO toDTO(VirtualBuddy buddy) {
        BuddyDTO dto = new BuddyDTO();
        dto.setId(buddy.getId());
        dto.setName(buddy.getName());
        dto.setAvatar(buddy.getAvatar());
        dto.setLevel(buddy.getLevel());
        dto.setExperience(buddy.getExperience());
        dto.setHappiness(buddy.getHappiness());
        dto.setMeditationStreak(buddy.getMeditationStreak());
        dto.setTotalMeditationMinutes(buddy.getTotalMeditationMinutes());
        dto.setLastHug(buddy.getLastHug());
        dto.setLastMeditation(buddy.getLastMeditation());
        dto.setCreatedAt(buddy.getCreatedAt());
        dto.setUpdatedAt(buddy.getUpdatedAt());
        //dto.setHabitat(pet.getHabitat());
        dto.setRewards(buddy.getRewards());
        dto.setSessionHistory(
                buddy.getSessionHistory().stream()
                        .map(session -> new MeditationSession(
                                session.getDate(),
                                session.getDuration(),
                                session.getReward(),
                                session.getHabitat()
                        ))
                        .collect(Collectors.toList())
        );
        dto.setOwnerId(buddy.getOwner() != null ? buddy.getOwner().getId() : null);

        dto.setAvatarStages(buildAvatarStages(buddy.getAvatar(), 4));
        return dto;
    }
}
