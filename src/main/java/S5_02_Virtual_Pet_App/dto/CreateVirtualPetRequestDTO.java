package S5_02_Virtual_Pet_App.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateVirtualPetRequestDTO {
    @NotBlank(message = "El nombre de la mascota no puede estar vacío")
    @Size(min = 2, max = 30, message = "El nombre debe tener entre 2 y 30 caracteres")
    private String name;

    @NotBlank(message = "El avatar no puede estar vacío")
    private String avatar;

    @NotBlank(message = "El ID del dueño no puede estar vacío")
    private String ownerId;
}
