package ci.jinx.qr_code.qrcode.dto.content;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UrlContent {
    @NotBlank(message = "L'URL est obligatoire")
    private String url;
}
