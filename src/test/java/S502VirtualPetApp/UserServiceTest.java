package S502VirtualPetApp;


import S502VirtualPetApp.dto.model.UserDTO;
import S502VirtualPetApp.model.Role;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.repository.UserRepository;
import S502VirtualPetApp.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void registerNewUser_shouldCreateUser_whenUsernameAndEmailAreUnique() {
        UserDTO dto = new UserDTO();
        dto.setUsername("hierba");
        dto.setEmail("hierba@email.com");
        dto.setPassword("1234");

        when(userRepository.existsByUsername("hierba")).thenReturn(false);
        when(userRepository.existsByEmail("hierba@email.com")).thenReturn(false);
        when(passwordEncoder.encode("1234")).thenReturn("encoded1234");

        User savedUser = new User("hierba", "hierba@email.com", "encoded1234", Set.of(Role.USER));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.registerNewUser(dto);

        assertEquals("hierba", result.getUsername());
        assertEquals("encoded1234", result.getPassword());
    }
}
