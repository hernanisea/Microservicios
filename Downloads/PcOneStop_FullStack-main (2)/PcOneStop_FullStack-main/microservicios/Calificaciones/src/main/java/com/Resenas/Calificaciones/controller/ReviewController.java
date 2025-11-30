package com.Resenas.Calificaciones.controller;

import com.Resenas.Calificaciones.dto.ApiResponse;
import com.Resenas.Calificaciones.dto.ReviewRequest;
import com.Resenas.Calificaciones.dto.ReviewResponse;
import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
// Nota: Cambiamos la base a /api para tener libertad en las sub-rutas
@RequestMapping("/api") 
@Tag(name = "Calificaciones", description = "Gestión de reseñas vinculadas a productos")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // 1. GET /api/products/:productId/reviews - Obtener reseñas (método mejorado con DTOs)
    @Operation(summary = "Obtener reseñas de un producto")
    @GetMapping("/products/{productId}/reviews")
    public ResponseEntity<?> getProductReviews(@PathVariable String productId) {
        try {
            List<ReviewResponse> reviews = reviewService.getReviewsByProductId(productId);
            
            if (reviews.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(
                    true, 200, "Este producto aún no tiene reseñas", List.of(), 0L));
            }
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Reseñas obtenidas exitosamente", reviews, (long) reviews.size()));
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("statusCode", 500);
            errorResponse.put("message", "Error al obtener reseñas: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Método alternativo que retorna Review directamente (para compatibilidad)
    @GetMapping("/products/{productId}/reviews/legacy")
    public ResponseEntity<ApiResponse<List<Review>>> getProductReviewsLegacy(@PathVariable String productId) {
        List<Review> reviews = reviewService.findByProductId(productId);
        
        if (reviews.isEmpty()) {
             return ResponseEntity.ok(new ApiResponse<>(
                    true, 200, "Este producto aún no tiene reseñas", List.of(), 0L));
        }
        
        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Reseñas obtenidas", reviews, (long) reviews.size()));
    }

    // 2. POST /api/products/:productId/reviews - Crear reseña (método mejorado con DTOs)
    @Operation(summary = "Crear una nueva reseña para un producto")
    @PostMapping("/products/{productId}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable String productId,
            @Valid @RequestBody ReviewRequest request) {
        try {
            // Asegurar que el productId del path coincida con el del request
            request.setProductId(productId);
            
            ReviewResponse review = reviewService.createReview(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Reseña creada exitosamente", review, 1L));
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("statusCode", 400);
            errorResponse.put("message", "Error al crear reseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    // Método alternativo que acepta Review directamente (para compatibilidad)
    @PostMapping("/products/{productId}/reviews/legacy")
    public ResponseEntity<ApiResponse<Review>> createReviewLegacy(
            @PathVariable String productId, 
            @RequestBody Review review) {
        try {
            Review savedReview = reviewService.create(productId, review);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Reseña creada exitosamente", savedReview, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    // 3. PUT /api/products/:productId/reviews/:reviewId - Actualizar reseña (método mejorado con DTOs)
    @Operation(summary = "Actualizar una reseña existente")
    @PutMapping("/products/{productId}/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable String productId,
            @PathVariable String reviewId,
            @Valid @RequestBody ReviewRequest request) {
        try {
            request.setProductId(productId);
            request.setId(reviewId);
            
            ReviewResponse review = reviewService.updateReview(reviewId, productId, request);
            
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Reseña actualizada exitosamente", review, 1L));
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("statusCode", 404);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("statusCode", 400);
            errorResponse.put("message", "Error al actualizar reseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    // Método alternativo que acepta Review directamente (para compatibilidad)
    @PutMapping("/products/{productId}/reviews/{reviewId}/legacy")
    public ResponseEntity<ApiResponse<Review>> updateReviewLegacy(
            @PathVariable String productId,
            @PathVariable String reviewId,
            @RequestBody Review review) {
        try {
            Review updatedReview = reviewService.update(reviewId, review);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Reseña actualizada", updatedReview, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }
    
    // 4. DELETE /api/products/:productId/reviews/:reviewId - Eliminar reseña (método mejorado)
    @Operation(summary = "Eliminar una reseña")
    @DeleteMapping("/products/{productId}/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable String productId,
            @PathVariable String reviewId) {
        try {
            reviewService.deleteReview(reviewId, productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ok", true);
            response.put("statusCode", 200);
            response.put("message", "Reseña eliminada exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("statusCode", 404);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("ok", false);
            errorResponse.put("statusCode", 500);
            errorResponse.put("message", "Error al eliminar reseña: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // Método alternativo para eliminar por ID directo (para compatibilidad)
    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Reseña eliminada", null, 0L));
    }
}