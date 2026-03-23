package ci.jinx.qr_code.qrcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class QrCodeRequest {

    @NotBlank(message = "Le type de contenu est obligatoire")
    private String contentType;
    @NotNull(message = "Le contenu est obligatoire")
    private Object contentData;
    private String foregroundColor = "#000000";
    private String backgroundColor = "#FFFFFF";

    @Min(value = 100, message = "La taille minimale est 100px")
    @Max(value = 1000, message = "La taille maximale est 1000px")
    private Integer size = 300;
    private String logoBase64;
}