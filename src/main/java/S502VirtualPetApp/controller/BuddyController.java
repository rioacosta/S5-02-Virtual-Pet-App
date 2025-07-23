package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.model.MeditationSessionDTO;
import S502VirtualPetApp.dto.buddyActions.CreateVirtualBuddyRequestDTO;
import S502VirtualPetApp.dto.buddyActions.MeditationRequestDTO;
import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.BuddyService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/buddys")
public class BuddyController {
    private static final Logger logger = LoggerFactory.getLogger(BuddyController.class);

    private final BuddyService buddyService;

    public BuddyController(BuddyService buddyService) {
        this.buddyService = buddyService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new buddy")
    public BuddyDTO createBuddy(@Valid @RequestBody CreateVirtualBuddyRequestDTO request,
                                @AuthenticationPrincipal User user) {
        logger.debug("Creating buddy: {}", request);
        return buddyService.createBuddy(request, user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Show a buddy")
    public BuddyDTO getMyBuddy(@PathVariable String id, @AuthenticationPrincipal User user) {
        logger.info("BuddyController - Buscando buddy con ID: {}", id);
        return buddyService.getBuddyByIdOwned(id, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modify a buddy")
    public BuddyDTO updateBuddy(@PathVariable String id, @RequestBody BuddyDTO dto,
                                @AuthenticationPrincipal User user) {
        logger.info("Updating buddy: {}", id, dto.getName());
        return buddyService.updateBuddy(id, dto, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a buddy")
    public ResponseEntity<Void> deleteBuddy(@PathVariable String id, @AuthenticationPrincipal User user) {
        logger.info("Deleting buddy: {}", id);
        buddyService.deleteBuddy(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/meditate")
    @Operation(summary = "Meditation session request")
    public BuddyDTO meditate(@PathVariable String id,
                             @Valid @RequestBody MeditationRequestDTO request,
                             @AuthenticationPrincipal User user) {
        logger.info("Starting meditation session for: {}", id);
        return buddyService.meditate(id, request.getMinutes(), request.getHabitat(), user);
    }

    @PostMapping("/{id}/hug")
    @Operation(summary = "Having a hug request")
    public BuddyDTO hug(@PathVariable String id,
                        @AuthenticationPrincipal User user) {
        logger.info("Hugging: {}", id);
        return buddyService.hug(id, user);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Show meditation sessions history")
    public List<MeditationSessionDTO> getMeditationHistory(@PathVariable String id,
                                                           @AuthenticationPrincipal User user) {
        logger.info("Session history for: {}", id);
        return buddyService.getMeditationHistoryDTO(id, user);
    }

    @GetMapping("/{id}/rewards")
    @Operation(summary = "Show reward list")
    public List<String> getRewards(@PathVariable String id,
                                   @AuthenticationPrincipal User user) {
        return buddyService.getRewards(id, user);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get buddy status")
    public BuddyDTO getFullStatus(@PathVariable String id,
                                  @AuthenticationPrincipal User user) {
        logger.info("Status for: {}", user.getId(), user.getUsername());
        return buddyService.getFullStatus(id, user);
    }

    @PatchMapping("/{id}/rewards")
    @Operation(summary = "Add reward to one buddy")
    public BuddyDTO addReward(@PathVariable String id,
                              @RequestBody Map<String, String> request,
                              @AuthenticationPrincipal User user) {
        logger.info("Adding reward for: {}", id);
        String reward = request.get("reward");
        return buddyService.addReward(id, reward, user);
    }

}
