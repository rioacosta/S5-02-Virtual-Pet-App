package S502VirtualPetApp.dto.buddyActions;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HugRequestDTO {
    @NotBlank(message = "User ID is mandatory for a hug")
    private String senderId;

}
