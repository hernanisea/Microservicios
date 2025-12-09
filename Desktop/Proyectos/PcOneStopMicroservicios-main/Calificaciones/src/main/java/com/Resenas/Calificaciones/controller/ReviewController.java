package com.Resenas.Calificaciones.controller;

import com.Resenas.Calificaciones.dto.ApiResponse;
import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.service.ReviewService;
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
import java.util.Locale;

@RestController
@RequestMapping("/api/v1/reviews")
@Tag(name = "Calificaciones y Reseñas", description = "Gestión de reseñas, ratings y opiniones de productos")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Agregar/Actualizar reseña
    @Operation(
        summary = "Agregar o actualizar reseña",
        description = "Guarda una calificación (1-5 estrellas) y comentario para un producto. " +
                      "Si el usuario ya calificó el producto, se actualiza la reseña existente (upsert)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Reseña guardada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Rating inválido (debe ser entre 1 y 5)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos de la reseña",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = Review.class),
                    examples = @ExampleObject(
                        name = "Ejemplo de reseña",
                        value = """
                            {
                                "productId": 1,
                                "userId": 4,
                                "rating": 5,
                                "comment": "Excelente producto, muy recomendado!"
                            }
                            """
                    )
                )
            )
            @RequestBody Review review
    ) {
        try {
            Review savedReview = reviewService.save(review);
            ApiResponse<Review> response = new ApiResponse<>(
                    true, HttpStatus.CREATED.value(), "Reseña guardada exitosamente", savedReview, 1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<Review> response = new ApiResponse<>(
                    false, HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            ApiResponse<Review> response = new ApiResponse<>(
                    false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al guardar reseña: " + e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Obtener reseñas por producto
    @Operation(
        summary = "Obtener reseñas de un producto",
        description = "Devuelve la lista completa de reseñas y calificaciones asociadas a un producto específico."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reseñas obtenidas exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "El producto aún no tiene reseñas"
        )
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<Review>>> getByProduct(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long productId
    ) {
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

    // Promedio de rating por producto
    @Operation(
        summary = "Obtener rating promedio de un producto",
        description = "Calcula y devuelve el promedio de calificaciones (1-5) de un producto específico. " +
                      "Si el producto no tiene calificaciones, devuelve null en el campo data."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Promedio calculado exitosamente o producto sin calificaciones (data será null)"
        )
    })
    @GetMapping("/product/{productId}/average")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long productId
    ) {
        Double average = reviewService.getAverageRating(productId);
        String message = average != null ? 
            "Rating promedio: " + String.format(Locale.US, "%.1f", average) + " estrellas" :
            "Este producto aún no tiene calificaciones";
        
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), message, average, 1L));
    }

    // Listar todas las reseñas
    @Operation(
        summary = "Listar todas las reseñas",
        description = "Obtiene todas las reseñas del sistema. Solo para uso administrativo."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de reseñas obtenida exitosamente"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Review>>> getAll() {
        List<Review> reviews = reviewService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), "Lista de todas las reseñas", reviews, (long) reviews.size()));
    }

    // Reseñas por usuario
    @Operation(
        summary = "Obtener reseñas de un usuario",
        description = "Devuelve todas las reseñas escritas por un usuario específico. " +
                      "Si el usuario no tiene reseñas, devuelve una lista vacía."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de reseñas del usuario obtenida (puede estar vacía si no tiene reseñas)"
        )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Review>>> getByUser(
            @Parameter(description = "ID del usuario", example = "4", required = true)
            @PathVariable Long userId
    ) {
        List<Review> reviews = reviewService.findByUserId(userId);
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), "Reseñas del usuario", reviews, (long) reviews.size()));
    }
}
