package ci.jinx.qr_code.qrcode;

import ci.jinx.qr_code.models.QrCodeType;
import ci.jinx.qr_code.models.User;
import ci.jinx.qr_code.qrcode.dto.QrCodeRequest;
import ci.jinx.qr_code.qrcode.dto.QrCodeResponse;
import ci.jinx.qr_code.repository.QrCodeTypeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/qrcode")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final QrCodeTypeRepository qrCodeTypeRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    @PostMapping("/generate")
    public ResponseEntity<QrCodeResponse> generate(
            @Valid @RequestBody QrCodeRequest request,
            @AuthenticationPrincipal User user

    ) {
        log.info("POST /api/qrcode/generate — user : {}", user.getEmail());
        QrCodeResponse response = qrCodeService.generate(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<QrCodeResponse>> getHistory(
            @AuthenticationPrincipal User user) {
        log.info("GET /api/qrcode/history — user : {}", user.getEmail());
        List<QrCodeResponse> history = qrCodeService.getHistory(user);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{type}")
    public ResponseEntity<List<QrCodeResponse>> getHistoryByType(
            @PathVariable String type,

            @AuthenticationPrincipal User user) {
        log.info("GET /api/qrcode/history/{} — user : {}", type, user.getEmail());
        List<QrCodeResponse> history = qrCodeService.getHistoryByType(user, type);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(
            @AuthenticationPrincipal User user) {
        log.info("GET /api/qrcode/stats — user : {}", user.getEmail());
        Map<String, Long> stats = qrCodeService.getStatsByType(user);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/types")
    public ResponseEntity<List<QrCodeType>> getTypes() {
        log.info("GET /api/qrcode/types");
        List<QrCodeType> types = qrCodeTypeRepository.findByIsActiveTrueOrderByIdAsc();
        return ResponseEntity.ok(types);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        log.info("DELETE /api/qrcode/{} — user : {}", id, user.getEmail());
        qrCodeService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        log.info("GET /api/v1/qrcode/images/{}", filename);
        Path filePath = Paths.get(uploadDir).resolve(filename);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = filename.endsWith(".svg")
                ? MediaType.parseMediaType("image/svg+xml")
                : MediaType.IMAGE_PNG;

        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }
}
