package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.ProductReport;
import com.Catalogo.Inventario.repository.ReportRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @Operation(summary = "Reportar un producto")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductReport>> createReport(@RequestBody ProductReport report) {
        ProductReport saved = reportRepository.save(report);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, 201, "Reporte enviado", saved, 1L));
    }

    @Operation(summary = "Ver cantidad de reportes de un producto")
    @GetMapping("/count/{productId}")
    public ResponseEntity<ApiResponse<Long>> getReportCount(@PathVariable Long productId) {
        long count = reportRepository.countByProductId(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Conteo obtenido", count, count));
    }
    
    // Endpoint para que el Admin vea todos los reportes (Opcional, para tu lista)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductReport>>> getAllReports() {
        List<ProductReport> list = reportRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Lista de reportes", list, (long)list.size()));
    }
}