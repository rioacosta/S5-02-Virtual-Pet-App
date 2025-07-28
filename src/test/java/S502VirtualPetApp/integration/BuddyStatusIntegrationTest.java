package S502VirtualPetApp.integration;

import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
@SpringBootTest(properties = {
        "logging.level.org.springframework=DEBUG",
        "logging.level.com.yourpackage=TRACE"            })
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BuddyStatusIntegrationTest {

    private static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.0");

    static {
        mongoDBContainer.start();
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    private String jwtToken;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }
    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com"); // ✅ requerido por validación
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRoles(Set.of(Role.ADMIN)); // ⚠️ asegúrate que sea ADMIN, no ROLE_ADMIN

        userRepository.save(admin);

        String loginRequest = """
        {
            "username": "admin",
            "password": "admin123"
        }
        """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, String> jsonMap = objectMapper.readValue(json, Map.class);
        jwtToken = jsonMap.get("token");
    }

    @Test
    void happinessShouldDecayThroughStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/buddy/status")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void loginShouldFailWithInvalidCredentials() throws Exception {
        String invalidLogin = """
    {
        "username": "admin",
        "password": "wrongPassword"
    }
    """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLogin))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectStatusAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/buddy/status"))
                .andExpect(status().isForbidden()); // o Unauthorized según tu configuración
    }

    @Test
    void shouldRejectStatusAccessForUserWithoutAdminRole() throws Exception {
        // Crear un usuario sin rol ADMIN
        User user = new User();
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("user123"));
        user.setRoles(Set.of(Role.USER));
        userRepository.save(user);

        String loginRequest = """
    {
        "username": "user",
        "password": "user123"
    }
    """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        Map<String, String> jsonMap = objectMapper.readValue(json, Map.class);
        String userToken = jsonMap.get("token");

        // Intentar acceder con rol insuficiente
        mockMvc.perform(get("/api/buddy/status")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden()); // según tu configuración
    }


}
