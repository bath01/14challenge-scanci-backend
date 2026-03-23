package ci.jinx.qr_code.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import ci.jinx.qr_code.models.QrCode;

@Repository
public interface QrCodeRepository extends JpaRepository<QrCode, Long> {
    List<QrCode> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<QrCode> findByIdAndUserId(Long id, Long userId);
    List<QrCode> findByUserIdAndContentTypeOrderByCreatedAtDesc(Long userId, String contentType);
}
