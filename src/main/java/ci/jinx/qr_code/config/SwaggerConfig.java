package ci.jinx.qr_code.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ScanCI — QR Code API")
                        .description("API de génération et gestion de QR codes (URL, TEXT, EMAIL, WIFI, VCARD)")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("ScanCI")
                                .email("contact@scanci.ci")))
                .servers(List.of(new Server().url(baseUrl).description("Serveur actif")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Entrez votre token JWT (sans le préfixe 'Bearer')")));
    }
}
