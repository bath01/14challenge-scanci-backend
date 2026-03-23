package ci.jinx.qr_code.qrcode.dto.content;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TextContent {
    @NotBlank(message = "Le texte est obligatoire")
    private String text;
}
