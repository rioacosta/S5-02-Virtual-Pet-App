package S502VirtualPetApp.integration;

import S502VirtualPetApp.dto.buddyActions.CreateVirtualBuddyRequestDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.repository.VirtualBuddyRepository;
import S502VirtualPetApp.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CreateBuddyControllerWithAuthIntegrationTest extends BaseMongoIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private VirtualBuddyRepository virtualBuddyRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private JwtUtil jwtUtil;

    @Autowired private PasswordEncoder passwordEncoder;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        virtualBuddyRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setUsername("tester");
        testUser.setEmail("tes@ter.com");
        testUser.setPassword(passwordEncoder.encode("1234567"));

        User savedUser = userRepository.save(testUser);
        jwtToken = jwtUtil.generateToken(savedUser);
    }

    @Test
    public void testCreateVirtualBuddyWithAuth() throws Exception {
        CreateVirtualBuddyRequestDTO buddyRequest = new CreateVirtualBuddyRequestDTO();
        buddyRequest.setName("Firulais");
        buddyRequest.setAvatar("Dog.png");

        mockMvc.perform(post("/api/buddys/create")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buddyRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Firulais"));
    }

    @Test
    public void testCreateVirtualBuddyWithoutAuth() throws Exception {
        CreateVirtualBuddyRequestDTO buddyRequest = new CreateVirtualBuddyRequestDTO();
        buddyRequest.setName("Firulais");
        buddyRequest.setAvatar("Dog.png");

        mockMvc.perform(post("/api/buddys/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buddyRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}