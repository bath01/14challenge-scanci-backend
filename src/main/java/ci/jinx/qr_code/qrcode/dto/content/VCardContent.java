package ci.jinx.qr_code.qrcode.dto.content;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VCardContent {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    private String phone;
    private String email;
    private String address;
    private String company;
}