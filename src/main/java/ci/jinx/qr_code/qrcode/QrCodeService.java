package ci.jinx.qr_code.qrcode;

import ci.jinx.qr_code.models.QrCode;
import ci.jinx.qr_code.models.User;
import ci.jinx.qr_code.qrcode.dto.QrCodeRequest;
import ci.jinx.qr_code.qrcode.dto.QrCodeResponse;
import ci.jinx.qr_code.qrcode.dto.content.*;
import ci.jinx.qr_code.repository.QrCodeRepository;
import ci.jinx.qr_code.repository.QrCodeTypeRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final ObjectMapper objectMapper;
    private final QrCodeTypeRepository qrCodeTypeRepository;

    public QrCodeResponse generate(QrCodeRequest request, User user) {
        log.info("Génération QR code type {} pour {}", request.getContentType(), user.getEmail());

        try {
            String content = buildContent(request);
            log.debug("Contenu QR code construit : {}", content);

            BitMatrix bitMatrix = generateBitMatrix(content, request.getSize());

            int foregroundColor = hexToInt(request.getForegroundColor());
            int backgroundColor = hexToInt(request.getBackgroundColor());

            byte[] pngImage = generatePng(bitMatrix, foregroundColor, backgroundColor);

            if (request.getLogoBase64() != null && !request.getLogoBase64().isEmpty()) {
                pngImage = addLogoToPng(pngImage, request.getLogoBase64());
                log.debug("Logo ajouté au QR code");
            }

            String svgImage = generateSvg(bitMatrix, request);

            String contentDataJson = objectMapper.writeValueAsString(request.getContentData());

            QrCode qrCode = QrCode.builder()
                    .user(user)
                    .contentType(request.getContentType().toUpperCase())
                    .contentData(contentDataJson)
                    .foregroundColor(request.getForegroundColor())
                    .backgroundColor(request.getBackgroundColor())
                    .size(request.getSize())
                    .pngImage(pngImage)
                    .svgImage(svgImage)
                    .build();

            if (request.getLogoBase64() != null && !request.getLogoBase64().isEmpty()) {
                byte[] logoBytes = Base64.getDecoder().decode(
                        request.getLogoBase64().replaceAll("data:image/[^;]+;base64,", ""));
                qrCode.setLogoImage(logoBytes);
            }

            QrCode saved = qrCodeRepository.save(qrCode);
            log.info("QR code sauvegardé avec l'id : {}", saved.getId());

            return buildResponse(saved, pngImage, svgImage);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du QR code : {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du QR code : " + e.getMessage());
        }
    }

    public List<QrCodeResponse> getHistory(User user) {
        log.info("Récupération historique complet pour : {}", user.getEmail());

        List<QrCode> qrCodes = qrCodeRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        log.info("{} QR codes trouvés pour {}", qrCodes.size(), user.getEmail());

        return qrCodes.stream()
                .map(qrCode -> buildResponse(
                        qrCode,
                        qrCode.getPngImage(),
                        qrCode.getSvgImage()))
                .toList();
    }

    public List<QrCodeResponse> getHistoryByType(User user, String contentType) {
        log.info("Récupération historique type {} pour : {}", contentType, user.getEmail());

        //List<String> validTypes = List.of("URL", "TEXT", "EMAIL", "WIFI", "VCARD");
        String type = contentType.toUpperCase();

        if (!qrCodeTypeRepository.existsByCodeAndIsActiveTrue(contentType.toUpperCase())) {
            log.warn("Type invalide demandé : {}", contentType);
            throw new RuntimeException("Type invalide ou désactivé : " + contentType);
        }

        List<QrCode> qrCodes = qrCodeRepository
                .findByUserIdAndContentTypeOrderByCreatedAtDesc(user.getId(), type);

        log.info("{} QR codes de type {} trouvés pour {}",
                qrCodes.size(), type, user.getEmail());

        return qrCodes.stream()
                .map(qrCode -> buildResponse(
                        qrCode,
                        qrCode.getPngImage(),
                        qrCode.getSvgImage()))
                .toList();
    }

    public Map<String, Long> getStatsByType(User user) {
        log.info("Récupération stats par type pour : {}", user.getEmail());

        List<QrCode> allQrCodes = qrCodeRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        Map<String, Long> stats = allQrCodes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        QrCode::getContentType,
                        java.util.stream.Collectors.counting()));

        log.info("Stats calculées : {}", stats);
        return stats;
    }

    public void delete(Long id, User user) {
        log.info("Suppression QR code id {} pour {}", id, user.getEmail());

        QrCode qrCode = qrCodeRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("QR code non trouvé"));

        qrCodeRepository.delete(qrCode);
        log.info("QR code supprimé avec succès");
    }

    // ---- Méthodes privées ----

    private String buildContent(QrCodeRequest request) throws Exception {
        String type = request.getContentType().toUpperCase();
        Object data = request.getContentData();

        return switch (type) {
            case "URL" -> {
                UrlContent url = objectMapper.convertValue(data, UrlContent.class);
                yield url.getUrl();
            }
            case "TEXT" -> {
                TextContent text = objectMapper.convertValue(data, TextContent.class);
                yield text.getText();
            }
            case "EMAIL" -> {
                EmailContent email = objectMapper.convertValue(data, EmailContent.class);
                yield String.format("mailto:%s?subject=%s&body=%s",
                        email.getTo(),
                        email.getSubject() != null ? email.getSubject() : "",
                        email.getBody() != null ? email.getBody() : "");
            }
            case "WIFI" -> {
                WifiContent wifi = objectMapper.convertValue(data, WifiContent.class);
                yield String.format("WIFI:T:%s;S:%s;P:%s;;",
                        wifi.getSecurity(),
                        wifi.getSsid(),
                        wifi.getPassword() != null ? wifi.getPassword() : "");
            }
            case "VCARD" -> {
                VCardContent vcard = objectMapper.convertValue(data, VCardContent.class);
                yield String.format("""
                        BEGIN:VCARD
                        VERSION:3.0
                        N:%s;%s
                        FN:%s %s
                        TEL:%s
                        EMAIL:%s
                        ADR:%s
                        ORG:%s
                        END:VCARD""",
                        vcard.getLastName(), vcard.getFirstName(),
                        vcard.getFirstName(), vcard.getLastName(),
                        vcard.getPhone() != null ? vcard.getPhone() : "",
                        vcard.getEmail() != null ? vcard.getEmail() : "",
                        vcard.getAddress() != null ? vcard.getAddress() : "",
                        vcard.getCompany() != null ? vcard.getCompany() : "");
            }
            default -> throw new RuntimeException("Type de contenu non supporté : " + type);
        };
    }

    private BitMatrix generateBitMatrix(String content, int size) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        return writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
    }

    private byte[] generatePng(BitMatrix bitMatrix, int foreground, int background)
            throws IOException {
        MatrixToImageConfig config = new MatrixToImageConfig(foreground, background);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream, config);
        return outputStream.toByteArray();
    }

    private byte[] addLogoToPng(byte[] pngImage, String logoBase64) throws IOException {

        String cleanBase64 = logoBase64.replaceAll("data:image/[^;]+;base64,", "");
        byte[] logoBytes = Base64.getDecoder().decode(cleanBase64);

        BufferedImage qrImage = ImageIO.read(new java.io.ByteArrayInputStream(pngImage));
        BufferedImage logoImage = ImageIO.read(new java.io.ByteArrayInputStream(logoBytes));

        int qrSize = qrImage.getWidth();
        int logoSize = qrSize / 5;

        Image scaledLogo = logoImage.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);
        BufferedImage resizedLogo = new BufferedImage(logoSize, logoSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resizedLogo.createGraphics();
        g2d.drawImage(scaledLogo, 0, 0, null);
        g2d.dispose();

        Graphics2D qrGraphics = qrImage.createGraphics();
        int logoX = (qrSize - logoSize) / 2;
        int logoY = (qrSize - logoSize) / 2;
        qrGraphics.drawImage(resizedLogo, logoX, logoY, null);
        qrGraphics.dispose();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    private String generateSvg(BitMatrix bitMatrix, QrCodeRequest request) {
        int size = bitMatrix.getWidth();
        int pixelSize = 10;

        StringBuilder svg = new StringBuilder();
        svg.append(String.format("""
                <svg xmlns="http://www.w3.org/2000/svg"
                     width="%d" height="%d"
                     viewBox="0 0 %d %d">
                """,
                size * pixelSize, size * pixelSize,
                size * pixelSize, size * pixelSize));
        svg.append(String.format(
                "<rect width=\"100%%\" height=\"100%%\" fill=\"%s\"/>%n",
                request.getBackgroundColor()));

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (bitMatrix.get(x, y)) {
                    svg.append(String.format(
                            "<rect x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" fill=\"%s\"/>%n",
                            x * pixelSize, y * pixelSize,
                            pixelSize, pixelSize,
                            request.getForegroundColor()));
                }
            }
        }

        svg.append("</svg>");
        return svg.toString();
    }

    private QrCodeResponse buildResponse(QrCode qrCode, byte[] pngImage, String svgImage) {
        String pngBase64 = pngImage != null ? Base64.getEncoder().encodeToString(pngImage) : null;
        String svgBase64 = svgImage != null ? Base64.getEncoder().encodeToString(svgImage.getBytes()) : null;

        return QrCodeResponse.builder()
                .id(qrCode.getId())
                .contentType(qrCode.getContentType())
                .contentData(qrCode.getContentData())
                .foregroundColor(qrCode.getForegroundColor())
                .backgroundColor(qrCode.getBackgroundColor())
                .size(qrCode.getSize())
                .pngBase64(pngBase64)
                .svgBase64(svgBase64)
                .createdAt(qrCode.getCreatedAt())
                .build();
    }

    private int hexToInt(String hex) {
        return (int) Long.parseLong(hex.replace("#", "FF"), 16);

    }
}
