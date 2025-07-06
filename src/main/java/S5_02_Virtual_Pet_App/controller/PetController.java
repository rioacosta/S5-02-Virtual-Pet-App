package S5_02_Virtual_Pet_App.controller;

import S5_02_Virtual_Pet_App.dto.CreateVirtualPetRequestDTO;
import S5_02_Virtual_Pet_App.dto.HugRequestDTO;
import S5_02_Virtual_Pet_App.dto.MeditationRequestDTO;
import S5_02_Virtual_Pet_App.dto.PetDTO;
import S5_02_Virtual_Pet_App.model.VirtualPet;
import S5_02_Virtual_Pet_App.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public void deletePet(@PathVariable String id) {
        petService.deletePet(id);
    }

    // Meditar
    @PostMapping("/{id}/meditate")
    public PetDTO meditate(@PathVariable String id, @RequestBody MeditationRequestDTO request) {
        return petService.meditate(id, request.getMinutes());
    }

    // Abrazar
    @PostMapping("/{id}/hug")
    public PetDTO hug(@PathVariable String id, @RequestBody(required = false) HugRequestDTO request) {
        PetDTO pet = petService.getPetById(id);
        VirtualPet petEntity = petService.getPetEntityById(id); // Este método aún no existe, lo vemos abajo
        petEntity.hug();
        return petService.updatePet(id, petService.toDTO(petEntity));
    }
}
