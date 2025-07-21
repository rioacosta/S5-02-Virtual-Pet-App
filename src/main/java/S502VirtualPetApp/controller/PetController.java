package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.model.MeditationSessionDTO;
import S502VirtualPetApp.dto.petActions.CreateVirtualPetRequestDTO;
import S502VirtualPetApp.dto.petActions.MeditationRequestDTO;
import S502VirtualPetApp.dto.model.PetDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    @Operation(summary = "Mostrar las mascotas del usuario")
    public List<PetDTO> getMyPets(@AuthenticationPrincipal User user) {
        return petService.getPetsByOwner(user);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Mostrar una mascota")
    public PetDTO getMyPet(@PathVariable String id, @AuthenticationPrincipal User user) {
        return petService.getPetByIdOwned(id, user);
    }

    @PostMapping
    @Operation(summary = "Crear una nueva mascota")
    public PetDTO createPet(@Valid @RequestBody CreateVirtualPetRequestDTO request,
                            @AuthenticationPrincipal User user) {
        return petService.createPet(request, user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar una mascota")
    public PetDTO updatePet(@PathVariable String id, @RequestBody PetDTO dto,
                            @AuthenticationPrincipal User user) {
        return petService.updatePet(id, dto, user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una mascota")
    public ResponseEntity<Void> deletePet(@PathVariable String id, @AuthenticationPrincipal User user) {
        petService.deletePet(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/meditate")
    @Operation(summary = "Solicitud de meditar")
    public PetDTO meditate(@PathVariable String id,
                           @Valid @RequestBody MeditationRequestDTO request,
                           @AuthenticationPrincipal User user) {
        return petService.meditate(id, request.getMinutes(), request.getHabitat(), user);
    }

    @PostMapping("/{id}/hug")
    @Operation(summary = "Solicitud de abrazo")
    public PetDTO hug(@PathVariable String id,
                      @AuthenticationPrincipal User user) {
        return petService.hug(id, user);
    }

    /*@PutMapping("/{id}/habitat")
    @Operation(summary = "Solicitud de cambio de habitat")
    public PetDTO changeHabitat(@PathVariable String id,
                                @RequestBody ChangeHabitatRequestDTO request,
                                @AuthenticationPrincipal User user) {
        return petService.changeHabitat(id, request.getHabitat(), user);
    }*/

    @GetMapping("/{id}/history")
    @Operation(summary = "Mostrar historial de sesiones de meditación")
    public List<MeditationSessionDTO> getMeditationHistory(@PathVariable String id,
                                                           @AuthenticationPrincipal User user) {
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
        return petService.getFullStatus(id, user);
    }

    @PatchMapping("/{id}/rewards")
    @Operation(summary = "Agregar una recompensa modular al buddy")
    public PetDTO addReward(@PathVariable String id,
                            @RequestBody Map<String, String> request,
                            @AuthenticationPrincipal User user) {
        String reward = request.get("reward");
        return petService.addReward(id, reward, user);
    }

}
