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
    private Integer size;
    private String pngUrl;
    private String svgUrl;
    private LocalDateTime createdAt;
}
