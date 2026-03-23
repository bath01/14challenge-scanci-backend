package ci.jinx.qr_code.qrcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeResponse {

    private Long id;
    private String contentType;
    private String contentData;

    private String foregroundColor;
    private String backgroundColor;
    private Integer size;
    private String pngBase64;
    private String svgBase64;

    private LocalDateTime createdAt;
}