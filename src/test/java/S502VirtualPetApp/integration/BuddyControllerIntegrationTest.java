package S502VirtualPetApp.integration;

import S502VirtualPetApp.dto.buddyActions.CreateVirtualBuddyRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BuddyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCreateVirtualBuddy() throws Exception {
        CreateVirtualBuddyRequestDTO buddyRequest = new CreateVirtualBuddyRequestDTO();
        buddyRequest.setName("Firulais");
        buddyRequest.setAvatar("Dog.png");

        mockMvc.perform(post("/api/pets/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buddyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.petName").value("Firulais"));
    }
}
