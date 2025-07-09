package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.habitat.ChangeHabitatRequestDTO;
import S502VirtualPetApp.dto.petActions.CreateVirtualPetRequestDTO;
import S502VirtualPetApp.dto.petActions.HugRequestDTO;
import S502VirtualPetApp.dto.petActions.MeditationRequestDTO;
import S502VirtualPetApp.dto.PetDTO;
import S502VirtualPetApp.model.MeditationSession;
import S502VirtualPetApp.model.VirtualPet;
import S502VirtualPetApp.service.PetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    // Obtener todas las mascotas
    @GetMapping
    public List<PetDTO> getAllPets() {
        return petService.getAllPets();
    }

    // Obtener mascotas por ID de dueño
    @GetMapping("/owner/{ownerId}")
    public List<PetDTO> getPetsByOwner(@PathVariable String ownerId) {
        return petService.getPetsByOwner(ownerId);
    }

    // Obtener mascota por su ID
    @GetMapping("/{id}")
    public PetDTO getPetById(@PathVariable String id) {
        return petService.getPetById(id);
    }

    // Crear nueva mascota
    @PostMapping
    public PetDTO createPet(@Valid @RequestBody CreateVirtualPetRequestDTO request) {
        PetDTO dto = new PetDTO();
        dto.setName(request.getName());
        dto.setAvatar(request.getAvatar());
        dto.setOwnerId(request.getOwnerId());
        return petService.createPet(dto);
    }

    // Actualizar mascota
    @PutMapping("/{id}")
    public PetDTO updatePet(@PathVariable String id, @RequestBody PetDTO dto) {
        return petService.updatePet(id, dto);
    }

    // Eliminar mascota
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(@PathVariable String id) {
        petService.deletePet(id);
        return ResponseEntity.ok().build();
    }

    // Meditar
    @PostMapping("/{id}/meditate")
    public PetDTO meditate(@PathVariable String id, @Valid @RequestBody MeditationRequestDTO request) {
        System.out.println("Meditated pet: " + petService.meditate(id, request.getMinutes()));
        return petService.meditate(id, request.getMinutes());
    }

    // Abrazar
    @PostMapping("/{id}/hug")
    public PetDTO hug(@PathVariable String id, @RequestBody(required = false) HugRequestDTO request) {
        VirtualPet petEntity = petService.getPetEntityById(id);
        petEntity.hug();
        return petService.updatePet(id, petService.toDTO(petEntity));
    }

    // 🌄 Cambiar hábitat
    @PutMapping("/{id}/habitat")
    public ResponseEntity<?> changeHabitat(@PathVariable String id, @RequestBody ChangeHabitatRequestDTO request) {
        VirtualPet pet = petService.getPetEntityById(id);
        pet.setHabitat(request.getHabitat());
        pet.setUpdatedAt(java.time.LocalDateTime.now());
        petService.updatePet(id, petService.toDTO(pet));
        return ResponseEntity.ok().build();
    }

    // 🎁 Ver recompensas obtenidas
    @GetMapping("/{id}/rewards")
    public List<String> getRewards(@PathVariable String id) {
        return petService.getPetEntityById(id).getRewards();
    }

    // 📜 Ver historial de sesiones
    @GetMapping("/{id}/history")
    public List<MeditationSession> getMeditationHistory(@PathVariable String id) {
        return petService.getPetEntityById(id).getSessionHistory();
    }

    // 📊 Ver estado completo de la mascota
    @GetMapping("/{id}/status")
    public VirtualPet getFullStatus(@PathVariable String id) {
        return petService.getPetEntityById(id);
    }
}
