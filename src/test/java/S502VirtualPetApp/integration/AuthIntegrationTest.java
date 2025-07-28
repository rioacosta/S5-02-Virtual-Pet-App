package S502VirtualPetApp.integration;

import S502VirtualPetApp.dto.registerAndLogin.LoginRequestDTO;
import S502VirtualPetApp.dto.registerAndLogin.RegisterUserRequestDTO;
import S502VirtualPetApp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest extends BaseMongoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        System.out.println("DEBUG - Base de datos limpiada. Usuarios: " + userRepository.count());
    }

    @Test
    void testUserRegistrationAndLoginFlow() throws Exception {
        // 1. Registro de nuevo usuario
        RegisterUserRequestDTO registerRequest = new RegisterUserRequestDTO(
                "testuser",
                "test@example.com",
                "Password123!"
        );

        // Actualizado para esperar 200 en lugar de 201
        MvcResult registerResult = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk()) // Cambiado de isCreated() a isOk()
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andReturn();

        System.out.println("DEBUG - Respuesta registro: " +
                registerResult.getResponse().getContentAsString());

        // 2. Login con credenciales válidas
        LoginRequestDTO loginRequest = new LoginRequestDTO();
                loginRequest.setUsername("testuser");
                loginRequest.setPassword("Password123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        System.out.println("DEBUG - Respuesta login: " +
                loginResult.getResponse().getContentAsString());

        String response = loginResult.getResponse().getContentAsString();
        assertTrue(response.contains("token"));

        // 3. Intento de acceso a recurso protegido
        String token = objectMapper.readTree(response).get("token").asText();

        mockMvc.perform(post("/api/buddys/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"TestBuddy\",\"avatar\":\"Cat.png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestBuddy"));
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        // Registro previo
        RegisterUserRequestDTO registerRequest = new RegisterUserRequestDTO("", "", "");
        registerRequest.setUsername("existinguser");
        registerRequest.setEmail("existing@example.com");
        registerRequest.setPassword("ValidPassword123!");

        MvcResult registerResult = mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("DEBUG - Register response: " +
                registerResult.getResponse().getContentAsString());

        // Login con credenciales incorrectas
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("existinguser");
        loginRequest.setPassword("WrongPassword!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password")) // Mensaje actualizado
                .andReturn();

        System.out.println("DEBUG - Respuesta login fallido: " +
                loginResult.getResponse().getContentAsString());
    }

    @Test
    void testLoginWithNonExistentUser() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
                loginRequest.setUsername("nonexistent");
                loginRequest.setPassword("anypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }
}