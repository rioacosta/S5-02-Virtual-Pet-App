package S502VirtualPetApp.controller;

import S502VirtualPetApp.config.TestSecurityConfig;
import S502VirtualPetApp.dto.PetDTO;
import S502VirtualPetApp.dto.petActions.CreateVirtualPetRequestDTO;
import S502VirtualPetApp.dto.petActions.MeditationRequestDTO;
import S502VirtualPetApp.security.JwtAuthFilter;
import S502VirtualPetApp.security.JwtUtil;
import S502VirtualPetApp.service.PetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetController.class)
@Import(TestSecurityConfig.class)
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PetService petService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @MockBean
    private JwtUtil jwtUtil;

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
        pet.setExperience(42);
        pet.setHappiness(80);
        pet.setHealth(100);
        return pet;
    }

    @Test
    void testCreatePet() throws Exception {
        CreateVirtualPetRequestDTO request = new CreateVirtualPetRequestDTO();
        request.setName("Neko");
        request.setAvatar("cat.png");
        request.setOwnerId("owner123");

        Mockito.when(petService.createPet(any())).thenReturn(samplePet());

        mockMvc.perform(post("/api/pets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neko"));
    }

    @Test
    void testDeletePet() throws Exception {
        mockMvc.perform(delete("/api/pets/123"))
                .andExpect(status().isOk());

        verify(petService).deletePet("123");
    }

    @Test
    void testGetAllPets() throws Exception {
        Mockito.when(petService.getAllPets()).thenReturn(Collections.singletonList(samplePet()));

        mockMvc.perform(get("/api/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Neko"));
    }

    @Test
    void testGetPetById() throws Exception {
        Mockito.when(petService.getPetById("123")).thenReturn(samplePet());

        mockMvc.perform(get("/api/pets/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neko"));
    }

    @Test
    void testMeditatePet() throws Exception {
        MeditationRequestDTO request = new MeditationRequestDTO();
        request.setMinutes(10);

        PetDTO updatedPet = samplePet();
        updatedPet.setExperience(100);
        updatedPet.setHappiness(90);

        Mockito.when(petService.meditate(eq("123"), eq(10))).thenReturn(updatedPet);

        mockMvc.perform(post("/api/pets/123/meditate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experience").value(100))
                .andExpect(jsonPath("$.happiness").value(90));
    }
}
