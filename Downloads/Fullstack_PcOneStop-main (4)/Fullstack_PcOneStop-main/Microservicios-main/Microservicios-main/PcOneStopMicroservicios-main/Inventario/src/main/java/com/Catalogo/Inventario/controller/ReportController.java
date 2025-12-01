package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.ProductReport;
import com.Catalogo.Inventario.repository.ReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/reports", "/api/reports"})
@Tag(name = "Reportes de Productos", description = "Sistema de reportes para que los clientes de PcOneStop puedan reportar componentes con problemas, información incorrecta o contenido inapropiado")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @Operation(
        summary = "Reportar un componente con problemas",
        description = "Permite a los clientes de PcOneStop reportar componentes que tienen problemas, información incorrecta, " +
                     "precios erróneos, imágenes incorrectas o cualquier otro problema. Los reportes son revisados por administradores. " +
                     "Requiere autenticación JWT."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Reporte enviado exitosamente. Será revisado por el equipo de administración.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 201, \"message\": \"Reporte enviado\", \"data\": {\"id\": 1, \"productId\": 5, \"userId\": 10, \"reason\": \"Precio incorrecto\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ProductReport>> createReport(@RequestBody ProductReport report) {
        ProductReport saved = reportRepository.save(report);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, 201, "Reporte enviado", saved, 1L));
    }

    @Operation(
        summary = "Consultar cantidad de reportes de un componente",
        description = "Obtiene el número total de reportes que tiene un componente específico en PcOneStop. " +
                     "Útil para identificar productos problemáticos. Este endpoint es público, no requiere autenticación."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Cantidad de reportes obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Conteo obtenido\", \"data\": 3, \"count\": 3}")
            )
        )
    })
    @Parameter(
        name = "productId",
        description = "ID del componente del cual se desea conocer la cantidad de reportes",
        required = true,
        example = "5"
    )
    @GetMapping("/count/{productId}")
    public ResponseEntity<ApiResponse<Long>> getReportCount(@PathVariable Long productId) {
        long count = reportRepository.countByProductId(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Conteo obtenido", count, count));
    }
    
    @Operation(
        summary = "Listar todos los reportes (Solo Administradores)",
        description = "Obtiene el listado completo de reportes de componentes en PcOneStop. " +
                     "Solo disponible para administradores. Útil para revisar y gestionar reportes de productos problemáticos. " +
                     "Requiere autenticación JWT con rol ADMIN."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de reportes obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Acceso denegado: se requiere rol de administrador",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductReport>>> getAllReports() {
        List<ProductReport> list = reportRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Lista de reportes", list, (long)list.size()));
    }
}