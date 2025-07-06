package S5_02_Virtual_Pet_App.controller;

import S5_02_Virtual_Pet_App.dto.petActions.CreateVirtualPetRequestDTO;
import S5_02_Virtual_Pet_App.dto.petActions.MeditationRequestDTO;
import S5_02_Virtual_Pet_App.dto.PetDTO;
import S5_02_Virtual_Pet_App.service.PetService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

@WebMvcTest(PetController.class)
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetService petService;

    @Autowired
    private ObjectMapper objectMapper;

    private PetDTO samplePet() {
        PetDTO pet = new PetDTO();
        pet.setId("123");
        pet.setName("Neko");
        pet.setAvatar("cat.png");
        pet.setOwnerId("owner123");
        pet.setCreatedAt(LocalDateTime.now());
        pet.setUpdatedAt(LocalDateTime.now());
        pet.setLevel(1);
        pet.setExperience(0);
        pet.setHappiness(50);
        pet.setHealth(100);
        return pet;
    }

    @Test
    void testGetAllPets() throws Exception {
        Mockito.when(petService.getAllPets()).thenReturn(Collections.singletonList(samplePet()));

        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Neko"));
    }

    @Test
    void testCreatePet() throws Exception {
        CreateVirtualPetRequestDTO request = new CreateVirtualPetRequestDTO();
        request.setName("Neko");
        request.setAvatar("cat.png");
        request.setOwnerId("owner123");

        PetDTO response = samplePet();

        Mockito.when(petService.createPet(any())).thenReturn(response);

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neko"));
    }

    @Test
    void testMeditatePet() throws Exception {
        MeditationRequestDTO request = new MeditationRequestDTO();
        request.setMinutes(10);

        PetDTO updatedPet = samplePet();
        updatedPet.setExperience(20);
        updatedPet.setHappiness(55);

        Mockito.when(petService.meditate(eq("123"), eq(10))).thenReturn(updatedPet);

        mockMvc.perform(post("/api/pets/123/meditate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experience").value(20))
                .andExpect(jsonPath("$.happiness").value(55));
    }

    @Test
    void testGetPetById() throws Exception {
        PetDTO pet = samplePet();

        Mockito.when(petService.getPetById("123")).thenReturn(pet);

        mockMvc.perform(get("/api/pets/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neko"));
    }

    @Test
    void testDeletePet() throws Exception {
        mockMvc.perform(delete("/api/pets/123"))
                .andExpect(status().isOk());

        Mockito.verify(petService).deletePet("123");
    }
}
