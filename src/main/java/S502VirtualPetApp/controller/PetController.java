package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.model.MeditationSessionDTO;
import S502VirtualPetApp.dto.petActions.CreateVirtualPetRequestDTO;
import S502VirtualPetApp.dto.petActions.MeditationRequestDTO;
import S502VirtualPetApp.dto.model.PetDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.PetService;
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
@RequestMapping("/api/pets")
public class PetController {
    private static final Logger logger = LoggerFactory.getLogger(PetController.class);

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    @Operation(summary = "Show user buddy´s")
    public List<PetDTO> getMyPets(@AuthenticationPrincipal User user) {
        return petService.getPetsByOwner(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Show a buddy")
    public PetDTO getMyPet(@PathVariable String id, @AuthenticationPrincipal User user) {
        logger.info("PetController - Buscando mascota con ID: {}", id);
        return petService.getPetByIdOwned(id, user);
    }

    @PostMapping
    @Operation(summary = "Create a new buddy")
    public PetDTO createPet(@Valid @RequestBody CreateVirtualPetRequestDTO request,
                            @AuthenticationPrincipal User user) {
        logger.debug("Creating buddy: {}", request);
        return petService.createPet(request, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modify a buddy")
    public PetDTO updatePet(@PathVariable String id, @RequestBody PetDTO dto,
                            @AuthenticationPrincipal User user) {
        logger.info("Updating buddy: {}", id, dto.getName());
        return petService.updatePet(id, dto, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a buddy")
    public ResponseEntity<Void> deletePet(@PathVariable String id, @AuthenticationPrincipal User user) {
        logger.info("Deleting buddy: {}", id);
        petService.deletePet(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/meditate")
    @Operation(summary = "Meditation session request")
    public PetDTO meditate(@PathVariable String id,
                           @Valid @RequestBody MeditationRequestDTO request,
                           @AuthenticationPrincipal User user) {
        logger.info("Starting meditation session for: {}", id);
        return petService.meditate(id, request.getMinutes(), request.getHabitat(), user);
    }

    @PostMapping("/{id}/hug")
    @Operation(summary = "Having a hug request")
    public PetDTO hug(@PathVariable String id,
                      @AuthenticationPrincipal User user) {
        logger.info("Hugging: {}", id);
        return petService.hug(id, user);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Mostrar historial de sesiones de meditación")
    public List<MeditationSessionDTO> getMeditationHistory(@PathVariable String id,
                                                           @AuthenticationPrincipal User user) {
        logger.info("Session history for: {}", id);
        return petService.getMeditationHistoryDTO(id, user);
    }

    @GetMapping("/{id}/rewards")
    @Operation(summary = "Mostrar lista de recompensas")
    public List<String> getRewards(@PathVariable String id,
                                   @AuthenticationPrincipal User user) {
        return petService.getRewards(id, user);
    }

    @GetMapping("/{id}/status")
    public PetDTO getFullStatus(@PathVariable String id,
                                @AuthenticationPrincipal User user) {
        logger.info("Status for: {}", user.getId(), user.getUsername());
        return petService.getFullStatus(id, user);
    }

    @PatchMapping("/{id}/rewards")
    @Operation(summary = "Add reward to one buddy")
    public PetDTO addReward(@PathVariable String id,
                            @RequestBody Map<String, String> request,
                            @AuthenticationPrincipal User user) {
        logger.info("Adding reward for: {}", id);
        String reward = request.get("reward");
        return petService.addReward(id, reward, user);
    }

}
