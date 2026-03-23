package ci.jinx.qr_code.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "qr_codes")
public class QrCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Type(JsonType.class)
    @Column(name = "content_data", nullable = false, columnDefinition = "jsonb")
    private String contentData;

    @Column(name = "size")
    private Integer size;

    @Column(name = "logo_image")
    private byte[] logoImage;

    @Column(name = "png_url")
    private String pngUrl;

    @Column(name = "svg_url")
    private String svgUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.size == null) this.size = 300;
    }
}
