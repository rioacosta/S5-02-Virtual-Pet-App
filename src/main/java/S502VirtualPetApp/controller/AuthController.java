package S502VirtualPetApp.controller;

import S502VirtualPetApp.dto.registerAndLogin.LoginRequestDTO;
import S502VirtualPetApp.dto.registerAndLogin.LoginResponseDTO;
import S502VirtualPetApp.model.User;
import S502VirtualPetApp.security.JwtUtil;
import S502VirtualPetApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        logger.info("Trying login for: {}", request.getUsername());
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();

            String token = jwtUtil.generateToken(user);
            userService.updateLastLogin(user.getUsername());
            logger.info("Login successful for: {}", request.getUsername());
            return ResponseEntity.ok(new LoginResponseDTO(token, user.getUsername(), user.getEmail()));

        } catch (BadCredentialsException e) {
            logger.warn("Invalid credentials for: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            logger.error("Login error for {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed", "details", e.getMessage()));
        }
    }

    @GetMapping("/debug")
    public ResponseEntity<?> debug() {
        logger.debug("Access to debug endpoint");
        return ResponseEntity.ok(userService.findAll());
    }

}
