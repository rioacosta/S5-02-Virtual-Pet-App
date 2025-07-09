package S5_02_Virtual_Pet_App.service;

import S5_02_Virtual_Pet_App.dto.UserDTO;
import S5_02_Virtual_Pet_App.model.Role;
import S5_02_Virtual_Pet_App.model.User;
import S5_02_Virtual_Pet_App.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
