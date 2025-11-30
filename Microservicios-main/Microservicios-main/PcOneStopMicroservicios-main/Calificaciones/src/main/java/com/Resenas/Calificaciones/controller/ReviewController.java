package com.Resenas.Calificaciones.controller;

import com.Resenas.Calificaciones.dto.ApiResponse;
import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Calificaciones", description = "Gestión de reseñas y estrellas de productos")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Operation(summary = "Agregar nueva reseña", description = "Guarda una calificación y comentario para un producto")
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> add(@RequestBody Review review) {
        try {
            Review savedReview = reviewService.save(review);
            ApiResponse<Review> response = new ApiResponse<>(
                    true, HttpStatus.CREATED.value(), "Reseña guardada exitosamente", savedReview, 1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Error de validación (rating inválido)
            ApiResponse<Review> response = new ApiResponse<>(
                    false, HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // Error general
            ApiResponse<Review> response = new ApiResponse<>(
                    false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al guardar reseña: " + e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Obtener reseñas por producto", description = "Devuelve la lista de opiniones asociadas a un ID de producto")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<Review>>> getByProduct(@PathVariable Long productId) {
        List<Review> reviews = reviewService.findByProductId(productId);

        if (reviews.isEmpty()) {
            ApiResponse<List<Review>> response = new ApiResponse<>(
                    false, HttpStatus.NO_CONTENT.value(), "Este producto aún no tiene reseñas", null, 0L);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }

        ApiResponse<List<Review>> response = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Reseñas obtenidas", reviews, (long) reviews.size());
        return ResponseEntity.ok(response);
    }
}