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

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reportes de Productos", description = "Gestión de reportes y denuncias de productos")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    // Crear reporte
    @Operation(
        summary = "Reportar un producto",
        description = "Permite a un usuario reportar un producto por razones como: falsificación, " +
                      "información incorrecta, producto dañado, etc."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Reporte enviado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error al procesar el reporte"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductReport>> createReport(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del reporte",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = ProductReport.class),
                    examples = @ExampleObject(
                        name = "Ejemplo de reporte",
                        value = """
                            {
                                "productId": 1,
                                "userId": 4,
                                "reason": "El producto no coincide con la descripción"
                            }
                            """
                    )
                )
            )
            @RequestBody ProductReport report
    ) {
        try {
            ProductReport saved = reportRepository.save(report);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Reporte enviado exitosamente", saved, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al enviar reporte: " + e.getMessage(), null, 0L));
        }
    }

    // Contar reportes por producto
    @Operation(
        summary = "Obtener cantidad de reportes de un producto",
        description = "Devuelve el número total de reportes recibidos por un producto específico. " +
                      "Útil para detectar productos problemáticos."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Conteo obtenido exitosamente"
        )
    })
    @GetMapping("/count/{productId}")
    public ResponseEntity<ApiResponse<Long>> getReportCount(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long productId
    ) {
        long count = reportRepository.countByProductId(productId);
        String message = count > 0 ? 
            "El producto tiene " + count + " reporte(s)" : 
            "El producto no tiene reportes";
        return ResponseEntity.ok(new ApiResponse<>(true, 200, message, count, count));
    }

    // Listar todos los reportes
    @Operation(
        summary = "Listar todos los reportes",
        description = "Obtiene la lista completa de reportes del sistema. Solo para uso administrativo."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de reportes obtenida exitosamente"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductReport>>> getAllReports() {
        List<ProductReport> list = reportRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Lista de reportes", list, (long) list.size()));
    }

    // Obtener reportes por producto
    @Operation(
        summary = "Obtener reportes de un producto específico",
        description = "Devuelve todos los reportes detallados de un producto, incluyendo razones y usuarios. " +
                      "Si el producto no tiene reportes, devuelve una lista vacía."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de reportes del producto obtenida (puede estar vacía si no tiene reportes)"
        )
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ProductReport>>> getReportsByProduct(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long productId
    ) {
        List<ProductReport> reports = reportRepository.findByProductId(productId);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Reportes del producto", reports, (long) reports.size()));
    }
}
