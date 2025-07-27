package S502VirtualPetApp.service;

import S502VirtualPetApp.dto.model.MeditationSessionDTO;
import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.dto.buddyActions.CreateVirtualBuddyRequestDTO;
import S502VirtualPetApp.model.MeditationSession;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualBuddy;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualBuddyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public List<BuddyDTO> getBuddysByOwner(User owner) {
        return virtualBuddyRepository.findByOwner(owner).stream()
                .peek(this::decayHappinessIfInactive)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BuddyDTO getBuddyByIdOwned(String petId, User user) {
        logger.info("Finding buddy with ID: {}", petId);
        VirtualBuddy buddy = virtualBuddyRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Virtual buddy not found"));

        boolean isAdmin = user.getRoles().contains(Role.ADMIN) ||
                user.getRoles().contains(Role.ADMIN);
        boolean isOwner = buddy.getOwner().getId().equals(user.getId());

        if (!isOwner && !isAdmin) {
            logger.warn("Unauthorized access attempt by user: {}", user.getId());
            throw new RuntimeException("Unauthorized access to buddy");
        }

        decayHappinessIfInactive(buddy);
        return toDTO(buddy);
    }

    private VirtualBuddy getAndValidateOwnership(String petId, User user) {
        VirtualBuddy buddy = virtualBuddyRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Virtual buddy not found"));

        // Verificar si es admin o dueño usando el enum
        boolean isAdmin = user.getRoles().contains(Role.ADMIN) ||
                user.getRoles().contains(Role.ADMIN);
        boolean isOwner = buddy.getOwner().getId().equals(user.getId());

        if (!isOwner && !isAdmin) {
            logger.warn("Unauthorized access attempt by user: {}", user.getId());
            throw new RuntimeException("Unauthorized access to buddy");
        }

        return buddy;
    }

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

    public BuddyDTO updateBuddy(String buddyId, BuddyDTO dto, User owner) {
        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);
        logger.info("Updating buddy: {} with avatar {}", dto.getId(), dto.getAvatar());
        buddy.setName(dto.getName());
        buddy.setAvatar(dto.getAvatar());
        buddy.setUpdatedAt(java.time.LocalDateTime.now());
        return toDTO(virtualBuddyRepository.save(buddy));
    }

    public void deleteBuddy(String buddyId, User owner) {
        logger.info("Deleting buddy, type: {}", buddyId);
        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);
        virtualBuddyRepository.delete(buddy);
    }

    public BuddyDTO meditate(String buddyId, int minutes, String habitat, User owner) {
        logger.info("Starting buddy meditation session: {}", buddyId, habitat);
        if (minutes < 1 || minutes > 120) {
            logger.warn("Session is too long to process, should be less tha 120 minutes: {}", minutes);
            throw new IllegalArgumentException("Session is too long");
        }

        VirtualBuddy buddy = getAndValidateOwnership(buddyId, owner);
        String reward = assignReward(minutes);
        buddy.meditate(minutes, reward, habitat);

        /*if (habitat != null && !habitat.isBlank()) {
            buddy.setHabitat(habitat);
        }*/

        return toDTO(virtualBuddyRepository.save(buddy));
    }

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
        // 1. Usar la fecha más reciente entre interacciones y última verificación
        LocalDateTime reference = Stream.of(
                        buddy.getLastHappinessCheck(),
                        buddy.getLastMeditation(),
                        buddy.getLastHug()
                )
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(buddy.getCreatedAt());

        // 2. Calcular días REALES de inactividad
        long daysInactive = ChronoUnit.DAYS.between(
                reference.toLocalDate(),
                LocalDate.now()
        );

        // 3. Aplicar decaimiento solo si han pasado días
        if (daysInactive > 0) {
            int decay = (int) (daysInactive * 5);
            int newHappiness = Math.max(0, buddy.getHappiness() - decay);
            buddy.setHappiness(newHappiness);

            // 4. Actualizar última verificación
            buddy.setLastHappinessCheck(LocalDateTime.now());
            buddy.setUpdatedAt(LocalDateTime.now());

            // 5. Guardar cambios en BD
            virtualBuddyRepository.save(buddy);  // ✅ Critical!
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

    private List<MeditationSession> convertSessionHistory(List<MeditationSession> sessions) {
        return sessions.stream()
                .map(s -> new MeditationSession(s.getDate(), s.getDuration(), s.getReward(), s.getHabitat()))
                .toList();
    }

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
        dto.setTotalExperience(buddy.getTotalExperience());
        dto.setHappiness(buddy.getHappiness());
        dto.setMeditationStreak(buddy.getMeditationStreak());
        dto.setTotalMeditationMinutes(buddy.getTotalMeditationMinutes());
        dto.setLastHug(buddy.getLastHug());
        dto.setLastMeditation(buddy.getLastMeditation());
        dto.setCreatedAt(buddy.getCreatedAt());
        dto.setUpdatedAt(buddy.getUpdatedAt());
        dto.setRewards(buddy.getRewards());
        dto.setSessionHistory(convertSessionHistory(buddy.getSessionHistory()));
        dto.setOwnerId(buddy.getOwner() != null ? buddy.getOwner().getId() : null);
        dto.setAvatarStages(buildAvatarStages(buddy.getAvatar(), 4));
        return dto;
    }
}
