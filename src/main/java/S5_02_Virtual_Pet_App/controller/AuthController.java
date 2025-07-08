package S5_02_Virtual_Pet_App.controller;

import S5_02_Virtual_Pet_App.dto.registerAndLogin.LoginRequestDTO;
import S5_02_Virtual_Pet_App.dto.registerAndLogin.LoginResponseDTO;
import S5_02_Virtual_Pet_App.model.User;
import S5_02_Virtual_Pet_App.security.JwtUtil;
import S5_02_Virtual_Pet_App.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            String primaryRole = user.getRoles().stream()
                    .map(Enum::name)
                    .findFirst()
                    .orElse("USER");

            String token = jwtUtil.generateToken(user.getUsername(), "ROLE_" + primaryRole);
            userService.updateLastLogin(user.getUsername());

            return ResponseEntity.ok(new LoginResponseDTO(token, user.getUsername(), user.getEmail()));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed", "details", e.getMessage()));
        }
    }


    @GetMapping("/debug")
    public ResponseEntity<?> debug() {
        return ResponseEntity.ok(userService.findAll());
    }

}
