package ci.jinx.qr_code.repository;

import ci.jinx.qr_code.models.QrCodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QrCodeTypeRepository extends JpaRepository<QrCodeType, Long> {
    List<QrCodeType> findByIsActiveTrueOrderByIdAsc();
    Optional<QrCodeType> findByCode(String code);
    boolean existsByCodeAndIsActiveTrue(String code);
}