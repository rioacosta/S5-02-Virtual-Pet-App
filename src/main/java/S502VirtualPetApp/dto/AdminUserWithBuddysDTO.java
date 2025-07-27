package S502VirtualPetApp.dto;

import S502VirtualPetApp.dto.model.BuddyDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminUserWithBuddysDTO {
    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private List<BuddyDTO> buddys;

}
