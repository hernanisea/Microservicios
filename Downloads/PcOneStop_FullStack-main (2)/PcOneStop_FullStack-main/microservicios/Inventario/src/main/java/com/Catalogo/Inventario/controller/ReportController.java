package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.ProductReport;
import com.Catalogo.Inventario.repository.ReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// Nota: Cambiamos la base a /api para coincidir con tu petición
@RequestMapping("/api")
@Tag(name = "Reportes", description = "Gestión de reportes de productos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    // 1. POST /api/products/:productId/reports - Crear reporte
    @Operation(summary = "Crear un reporte para un producto", description = "Permite a los usuarios reportar problemas con un producto (requiere autenticación JWT)",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Reporte enviado exitosamente. Tu reporte ha sido registrado y será revisado por nuestro equipo."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado. Debes iniciar sesión y proporcionar un token JWT válido para reportar un producto."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado. El ID del producto proporcionado no existe en el catálogo.")
    })
    @PostMapping("/products/{productId}/reports")
    public ResponseEntity<ApiResponse<ProductReport>> createReport(
            @PathVariable String productId,
            @RequestBody ProductReport report) {
        
        // Asignamos el ID de la URL al objeto
        report.setProductId(productId);
        
        ProductReport saved = reportRepository.save(report);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, 201, "Reporte enviado exitosamente", saved, 1L));
    }

    // Endpoints adicionales útiles para admin
    
    @Operation(summary = "Ver reportes de un producto", description = "Obtiene todos los reportes realizados sobre un producto específico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reportes obtenidos exitosamente. Se devuelve la lista de reportes relacionados con el producto solicitado.")
    })
    @GetMapping("/products/{productId}/reports")
    public ResponseEntity<ApiResponse<List<ProductReport>>> getReportsByProduct(@PathVariable String productId) {
        List<ProductReport> reports = reportRepository.findByProductId(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Reportes del producto", reports, (long) reports.size()));
    }
    
    @Operation(summary = "Ver todos los reportes (Admin)", description = "Solo disponible para administradores",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de reportes obtenida exitosamente. Se devuelven todos los reportes del sistema para revisión administrativa."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado. Debes iniciar sesión y proporcionar un token JWT válido."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado. Este endpoint solo está disponible para usuarios con rol de administrador.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ProductReport>>> getAllReports() {
        List<ProductReport> list = reportRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Lista total de reportes", list, (long) list.size()));
    }
}