package S502VirtualPetApp.integration;

import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BuddyStatusIntegrationTest extends BaseMongoIntegrationTest {

    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.0");

    static { mongoDBContainer.start(); }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private String adminBuddyId;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // Crear usuario admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(Set.of(Role.ADMIN));
        userRepository.save(admin);

        // Login como admin
        String loginRequest = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        // Obtener token
        String json = loginResult.getResponse().getContentAsString();
        Map<String, String> jsonMap = objectMapper.readValue(json, Map.class);
        jwtToken = jsonMap.get("token");

        // Crear buddy para admin
        String createBuddyRequest = "{\"name\":\"AdminBuddy\",\"avatar\":\"Cat.png\"}";
        MvcResult createResult = mockMvc.perform(post("/api/buddys/create")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBuddyRequest))
                .andExpect(status().isOk())
                .andReturn();

        // Obtener ID del buddy creado
        String responseBody = createResult.getResponse().getContentAsString();
        System.out.println("DEBUG - Buddy creado: " + responseBody);

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        adminBuddyId = jsonNode.get("id").asText();

        // Verifica que el buddy tenga owner ID
        String ownerId = jsonNode.get("ownerId").asText();
        String adminId = admin.getId(); // Asegúrate de tener esta variable

        System.out.println("DEBUG - Buddy owner ID: " + ownerId);
        System.out.println("DEBUG - Admin ID: " + adminId);

        if (!ownerId.equals(adminId)) {
            throw new AssertionError("Buddy its not owned by user admin");
        }
    }

    @Test
    void happinessShouldDecayThroughStatusEndpoint() throws Exception {
        // Usar el ID almacenado en la ruta
        mockMvc.perform(get("/api/buddys/" + adminBuddyId + "/status")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void loginShouldFailWithInvalidCredentials() throws Exception {
        String invalidLogin = "{\"username\":\"admin\",\"password\":\"wrongPassword\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLogin))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectStatusAccessWithoutToken() throws Exception {
        // Usar el ID almacenado en la ruta
        mockMvc.perform(get("/api/buddys/" + adminBuddyId + "/status"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectStatusAccessForUserWithoutOwnership() throws Exception {
        // Crear usuario regular
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);

        // Login como usuario regular
        String loginRequest = "{\"username\":\"user\",\"password\":\"user123\"}";
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        // Obtener token de usuario regular
        String json = loginResult.getResponse().getContentAsString();
        Map<String, String> jsonMap = objectMapper.readValue(json, Map.class);
        String userToken = jsonMap.get("token");

        // Intentar acceder al buddy del ADMIN (no del usuario)
        mockMvc.perform(get("/api/buddys/" + adminBuddyId + "/status")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
    }
}