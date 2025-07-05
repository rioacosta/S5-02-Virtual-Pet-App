package S5_02_Virtual_Pet_App.dto;

import lombok.Data;

@Data
public class CreateVirtualPetRequestDTO {
    private String name;
    private String avatar;
    private String ownerId;
}
