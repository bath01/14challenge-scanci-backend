package ci.jinx.qr_code.qrcode.dto.content;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WifiContent {

    @NotBlank(message = "Le nom du réseau est obligatoire")
    private String ssid;
    private String password;
    private String security = "WPA";
}