package ci.jinx.qr_code.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "qr_code_types")
public class QrCodeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;        

    @Column(nullable = false)
    private String label;       

    @Column
    private String description; 

    @Column
    private String icon;        

    @Column(name = "is_active")
    private Boolean isActive;   
}