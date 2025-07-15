package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.habitat.ChangeHabitatRequestDTO;
import S502VirtualPetApp.dto.petActions.CreateVirtualPetRequestDTO;
import S502VirtualPetApp.dto.petActions.HugRequestDTO;
import S502VirtualPetApp.dto.petActions.MeditationRequestDTO;
import S502VirtualPetApp.dto.PetDTO;
import S502VirtualPetApp.model.MeditationSession;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualPet;
import S502VirtualPetApp.repository.VirtualPetRepository;
import S502VirtualPetApp.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    VirtualPetRepository virtualPetRepository;
    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    public List<PetDTO> getMyPets(@AuthenticationPrincipal User user) {
        return petService.getPetsByOwner(user);
    }

    @GetMapping("/{id}")
    public PetDTO getMyPet(@PathVariable String id, @AuthenticationPrincipal User user) {
        return petService.getPetByIdOwned(id, user);
    }

    @PostMapping
    public PetDTO createPet(@Valid @RequestBody CreateVirtualPetRequestDTO request,
                            @AuthenticationPrincipal User user) {
        return petService.createPet(request, user);
    }

    @PutMapping("/{id}")
    public PetDTO updatePet(@PathVariable String id, @RequestBody PetDTO dto,
                            @AuthenticationPrincipal User user) {
        return petService.updatePet(id, dto, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable String id, @AuthenticationPrincipal User user) {
        petService.deletePet(id, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/meditate")
    public PetDTO meditate(@PathVariable String id,
                           @Valid @RequestBody MeditationRequestDTO request,
                           @AuthenticationPrincipal User user) {
        return petService.meditate(id, request.getMinutes(), user);
    }

    @PostMapping("/{id}/hug")
    public PetDTO hug(@PathVariable String id,
                      @AuthenticationPrincipal User user) {
        return petService.hug(id, user);
    }

    @PutMapping("/{id}/habitat")
    public PetDTO changeHabitat(@PathVariable String id,
                                @RequestBody ChangeHabitatRequestDTO request,
                                @AuthenticationPrincipal User user) {
        return petService.changeHabitat(id, request.getHabitat(), user);
    }

    @GetMapping("/{id}/rewards")
    public List<String> getRewards(@PathVariable String id,
                                   @AuthenticationPrincipal User user) {
        return petService.getRewards(id, user);
    }

    @GetMapping("/{id}/history")
    public List<MeditationSession> getMeditationHistory(@PathVariable String id,
                                                        @AuthenticationPrincipal User user) {
        return petService.getMeditationHistory(id, user);
    }

    @GetMapping("/{id}/status")
    public PetDTO getFullStatus(@PathVariable String id,
                                @AuthenticationPrincipal User user) {
        return petService.getFullStatus(id, user);
    }

}
