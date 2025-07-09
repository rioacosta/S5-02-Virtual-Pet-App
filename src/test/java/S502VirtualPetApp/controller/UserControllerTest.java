package S502VirtualPetApp.controller;

import S502VirtualPetApp.config.TestSecurityConfig;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.security.JwtAuthFilter;
import S502VirtualPetApp.security.JwtUtil;
import S502VirtualPetApp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void register_shouldReturnUserDTO_whenValidRequest() throws Exception {
        User user = new User("hierba", "hierba@email.com", "encoded1234", Set.of(Role.USER));
        when(userService.registerNewUser(any())).thenReturn(user);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "hierba",
                                "email": "hierba@email.com",
                                "password": "1234"
                            }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("hierba"))
                .andExpect(jsonPath("$.email").value("hierba@email.com"));
    }
}
