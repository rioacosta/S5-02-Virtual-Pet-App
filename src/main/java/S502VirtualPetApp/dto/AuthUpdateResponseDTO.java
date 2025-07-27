package S502VirtualPetApp.dto;

import S502VirtualPetApp.dto.model.UserDTO;

public class AuthUpdateResponseDTO {
    private UserDTO user;
    private String token;

    public AuthUpdateResponseDTO(UserDTO user, String token) {
        this.user = user;
        this.token = token;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
