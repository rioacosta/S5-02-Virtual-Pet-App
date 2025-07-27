package S502VirtualPetApp.dto.buddyActions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVirtualBuddyRequestDTO {
    @NotBlank(message = "Buddy name must be present")
    @Size(min = 2, max = 30, message = "Name should be between 2 and 30 characters")
    private String name;

    @NotBlank(message = "Avatar can't be blank")
    private String avatar;
}
