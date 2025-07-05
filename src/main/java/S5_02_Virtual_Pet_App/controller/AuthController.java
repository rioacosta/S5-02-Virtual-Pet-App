package S5_02_Virtual_Pet_App.controller;

import S5_02_Virtual_Pet_App.dto.LoginRequestDTO;
import S5_02_Virtual_Pet_App.dto.LoginResponseDTO;
import S5_02_Virtual_Pet_App.model.User;
import S5_02_Virtual_Pet_App.security.JwtUtil;
import S5_02_Virtual_Pet_App.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();

        // Generar token usando JwtUtil y rol principal
        String primaryRole = user.getRoles().stream()
                .map(Enum::name)
                .findFirst()
                .orElse("USER");

        String token = jwtUtil.generateToken(user.getUsername(), "ROLE_" + primaryRole);

        // Actualizar último login
        userService.updateLastLogin(user.getUsername());

        return ResponseEntity.ok(
                new LoginResponseDTO(token, user.getUsername(), user.getEmail())
        );
    }
}
