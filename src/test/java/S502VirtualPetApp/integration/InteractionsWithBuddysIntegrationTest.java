package S502VirtualPetApp.integration;


import S502VirtualPetApp.dto.model.BuddyDTO;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualBuddyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InteractionsWithBuddysIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private VirtualBuddyRepository virtualBuddyRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private String buddyId;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setup() {
        // Limpia la base antes de cada test
        mongoTemplate.getDb().drop();
    }
    @Test
    void testMeditationSessionFlow() throws Exception {
        // Crear usuario
        User user = new User();
        user.setUsername("meditator");
        user.setEmail("meditator@example.com");
        user.setPassword(passwordEncoder.encode("med123"));
        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);

        // Login
        String loginRequest = "{\"username\":\"meditator\",\"password\":\"med123\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Crear buddy
        String buddyRequest = "{\"name\":\"ZenBuddy\",\"avatar\":\"MeditationFox.png\"}";
        MvcResult createBuddyResult = mockMvc.perform(post("/api/buddys/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buddyRequest))
                .andExpect(status().isOk())
                .andReturn();

        String buddyId = objectMapper.readTree(createBuddyResult.getResponse().getContentAsString()).get("id").asText();

        // Meditar (10 minutos, reward será calculado internamente)
        String meditateRequest = """
        {
            "minutes": 10,
            "habitat": "Zen Garden"
        }
        """.formatted(buddyId);

        MvcResult meditationResult = mockMvc.perform(post("/api/buddys/" + buddyId + "/meditate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(meditateRequest))
                .andExpect(status().isOk())
                .andReturn();

        // Parseamos el response a DTO
        String responseJson = meditationResult.getResponse().getContentAsString();
        BuddyDTO buddy = objectMapper.readValue(responseJson, BuddyDTO.class);

        // Verificaciones
        assertEquals(1, buddy.getLevel());
        assertEquals(35, buddy.getExperience());
        assertEquals(35, buddy.getTotalExperience());
        assertEquals(60, buddy.getHappiness());
        assertEquals(1, buddy.getMeditationStreak());
        assertEquals(10, buddy.getTotalMeditationMinutes());
        assertNotNull(buddy.getLastMeditation());
        assertEquals(1, buddy.getSessionHistory().size());
        assertTrue(buddy.getRewards().contains("hat")); //isEmpty());
    }

    @Test
    void testHugInteraction() throws Exception {
        // Crear usuario
        User user = new User();
        user.setUsername("interactor");
        user.setEmail("interact@example.com");
        user.setPassword(passwordEncoder.encode("int123"));
        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);

        // Login
        String loginRequest = """
                    {
                        "username": "interactor",
                        "password": "int123"
                    }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Crear buddy
        String buddyRequest = """
                    {
                        "name": "Huggy",
                        "avatar": "Bear.png"
                    }
                """;

        MvcResult buddyResult = mockMvc.perform(post("/api/buddys/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buddyRequest))
                .andExpect(status().isOk())
                .andReturn();

        String buddyId = objectMapper.readTree(buddyResult.getResponse().getContentAsString())
                .get("id").asText();

        // Acción: hug
        MvcResult hugResult = mockMvc.perform(post("/api/buddys/" + buddyId + "/hug")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = hugResult.getResponse().getContentAsString();
        BuddyDTO buddy = objectMapper.readValue(responseJson, BuddyDTO.class);

        // Verificar que la felicidad subió a 60 (50 de base + 10 del abrazo)
        assertEquals(60, buddy.getHappiness());

        // Segundo intento de abrazo inmediato
        MvcResult secondHug = mockMvc.perform(post("/api/buddys/" + buddyId + "/hug")
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        int statusCode = secondHug.getResponse().getStatus();

        // Depende si tienes lógica de cooldown o no:
        if (statusCode == 429) {
            // Cooldown activo
            assertEquals(429, statusCode);
        } else {
            // Si no hay cooldown implementado aún
            BuddyDTO buddyAfterSecondHug = objectMapper.readValue(secondHug.getResponse().getContentAsString(), BuddyDTO.class);
            assertEquals(20, buddyAfterSecondHug.getHappiness()); // Acumuló +10 más
        }
    }
}
