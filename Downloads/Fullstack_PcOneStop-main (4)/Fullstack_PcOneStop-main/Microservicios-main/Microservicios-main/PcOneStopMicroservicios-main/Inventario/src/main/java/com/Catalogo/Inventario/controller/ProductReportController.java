package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.ProductReport;
import com.Catalogo.Inventario.repository.ReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Reportes de Productos", description = "Sistema de reportes para que los clientes de PcOneStop puedan reportar componentes con problemas, información incorrecta o contenido inapropiado")
public class ProductReportController {

    private static final Logger logger = LoggerFactory.getLogger(ProductReportController.class);

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(
        summary = "Reportar un componente específico con problemas",
        description = "Permite a los clientes de PcOneStop reportar un componente específico que tiene problemas, " +
                     "información incorrecta, precio erróneo, imagen incorrecta o cualquier otro inconveniente. " +
                     "El productId se toma de la URL automáticamente. Los reportes son revisados por administradores. " +
                     "Requiere autenticación JWT. Este endpoint es compatible con la estructura de rutas del frontend."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Reporte enviado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 201, \"message\": \"Reporte enviado\", \"data\": {\"id\": 1, \"productId\": 5, \"userId\": 10, \"reason\": \"Precio incorrecto\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error de validación: datos faltantes o inválidos",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El motivo del reporte es obligatorio\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido. Incluye el token en el header 'Authorization: Bearer <token>'",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 401, \"message\": \"Se requiere token de autorización\", \"data\": null, \"count\": 0}")
            )
        )
    })
    @Parameter(
        name = "productId",
        description = "ID del componente de PC que se desea reportar (se toma de la URL)",
        required = true,
        example = "5"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos del reporte. El productId se toma automáticamente de la URL. El userId es obligatorio (puede extraerse del token JWT). El reason es opcional pero recomendado.",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ProductReport.class),
            examples = {
                @ExampleObject(
                    name = "default",
                    summary = "Reporte con motivo (por defecto)",
                    value = "{\"userId\":10,\"reason\":\"Precio incorrecto en el sitio web\"}"
                ),
                @ExampleObject(
                    name = "Reporte sin motivo",
                    summary = "Solo con userId",
                    value = "{\"userId\":10}"
                )
            }
        )
    )
    @PostMapping("/{productId}/reports")
    public ResponseEntity<ApiResponse<ProductReport>> createReportForProduct(
            @PathVariable Long productId,
            HttpServletRequest request) {
        String jsonBody = null;
        try {
            logger.info("=== INICIO CREAR REPORTE ===");
            logger.info("productId desde URL: {}", productId);
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Content-Length: {}", request.getContentLength());
            
            // LEER EL BODY DIRECTAMENTE DESDE EL REQUEST (MISMA SOLUCIÓN QUE OTROS ENDPOINTS)
            try (BufferedReader reader = request.getReader()) {
                jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                logger.info("=== JSON BODY RAW (CREAR REPORTE) ===");
                logger.info("Body recibido: {}", jsonBody);
            } catch (IOException e) {
                logger.error("ERROR al leer el body del request: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al leer el cuerpo de la petición: " + e.getMessage(), null, 0L));
            }
            
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("ERROR CRÍTICO: El JSON body está vacío o es NULL!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error: El cuerpo de la petición está vacío", null, 0L));
            }
            
            // Deserializar manualmente
            ProductReport report;
            try {
                report = objectMapper.readValue(jsonBody, ProductReport.class);
                logger.info("=== DESERIALIZACIÓN REPORTE ===");
                logger.info("Report deserializado: {}", report);
                logger.info("userId: {}", report.getUserId());
                logger.info("reason: '{}'", report.getReason());
            } catch (Exception e) {
                logger.error("ERROR al deserializar JSON: {}", e.getMessage(), e);
                logger.error("JSON que falló: {}", jsonBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al procesar el JSON: " + e.getMessage(), null, 0L));
            }
            
            // Asegurar que el productId en el body coincida con el de la URL
            report.setProductId(productId);
            logger.info("Guardando reporte en la base de datos...");
            ProductReport saved = reportRepository.save(report);
            logger.info("Reporte guardado con ID: {}", saved.getId());
            logger.info("=== REPORTE CREADO EXITOSAMENTE - ID: {} ===", saved.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Reporte enviado", saved, 1L));
        } catch (Exception e) {
            logger.error("=== ERROR AL CREAR REPORTE ===", e);
            logger.error("Tipo de excepción: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al enviar el reporte: " + e.getMessage(), null, 0L));
        }
    }
}

