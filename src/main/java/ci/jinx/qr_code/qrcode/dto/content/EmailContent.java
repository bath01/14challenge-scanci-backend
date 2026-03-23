package ci.jinx.qr_code.qrcode.dto.content;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailContent {

    @NotBlank(message = "Le destinataire est obligatoire")
    @Email(message = "Format email invalide")
    private String to;

    private String subject;
    private String body;
}