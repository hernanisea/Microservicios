package com.Resenas.Calificaciones.controller;

import com.Resenas.Calificaciones.dto.ApiResponse;
import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/reviews", "/api/products"})
@Tag(name = "Reseñas y Calificaciones", description = "Sistema de reseñas para que los clientes de PcOneStop puedan calificar y opinar sobre los componentes de PC que han comprado. Ayuda a otros clientes a tomar decisiones de compra.")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(
        summary = "Dejar una reseña de un componente PC",
        description = "Permite a los clientes de PcOneStop calificar y comentar sobre componentes de computadora que han comprado. " +
                     "La calificación va de 1 a 5 estrellas (1 = Muy malo, 2 = Malo, 3 = Regular, 4 = Bueno, 5 = Excelente) " +
                     "y se puede incluir un comentario opcional con la experiencia de uso, pros, contras y recomendaciones. " +
                     "Las reseñas ayudan a otros clientes a tomar decisiones informadas antes de comprar. " +
                     "Requiere autenticación JWT. El userId se puede extraer del token o proporcionarse en el body."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Reseña guardada exitosamente. Aparecerá en la página del producto para que otros clientes la vean.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 201, \"message\": \"Reseña guardada exitosamente\", \"data\": {\"id\": 1, \"productId\": 5, \"userId\": 10, \"rating\": 5, \"comment\": \"Excelente GPU, muy buena para gaming\", \"date\": \"2024-01-15\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error de validación: rating fuera del rango 1-5, userId faltante, productId faltante, o datos incompletos",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Rating inválido", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El rating debe estar entre 1 y 5\", \"data\": null, \"count\": 0}"),
                    @ExampleObject(name = "Usuario faltante", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El ID del usuario (userId) es obligatorio para crear una reseña.\", \"data\": null, \"count\": 0}"),
                    @ExampleObject(name = "Producto faltante", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El ID del producto (productId) es obligatorio para crear una reseña.\", \"data\": null, \"count\": 0}")
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error al guardar la reseña",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos de la reseña. Campos obligatorios: productId, userId, rating (1-5). El comment es opcional pero recomendado.",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Review.class),
            examples = {
                @ExampleObject(
                    name = "default",
                    summary = "Reseña completa (por defecto)",
                    value = "{\"productId\":5,\"userId\":10,\"rating\":5,\"comment\":\"Excelente GPU, muy buena para gaming en 1440p. Llegó a tiempo y en perfecto estado. La recomiendo totalmente.\"}"
                ),
                @ExampleObject(
                    name = "Reseña simple",
                    summary = "Solo con rating",
                    value = "{\"productId\":5,\"userId\":10,\"rating\":4}"
                )
            }
        )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> add(HttpServletRequest request) {
        String jsonBody = null;
        try {
            logger.info("=== INICIO CREAR RESEÑA ===");
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Content-Length: {}", request.getContentLength());
            
            // LEER EL BODY DIRECTAMENTE DESDE EL REQUEST (MISMA SOLUCIÓN QUE OTROS ENDPOINTS)
            try (BufferedReader reader = request.getReader()) {
                jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                logger.info("=== JSON BODY RAW (CREAR RESEÑA) ===");
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
            Review review;
            try {
                review = objectMapper.readValue(jsonBody, Review.class);
                logger.info("=== DESERIALIZACIÓN RESEÑA ===");
                logger.info("Review deserializado: {}", review);
                logger.info("userId: {}", review.getUserId());
                logger.info("productId: {}", review.getProductId());
                logger.info("rating: {}", review.getRating());
                logger.info("comment: '{}'", review.getComment());
            } catch (Exception e) {
                logger.error("ERROR al deserializar JSON: {}", e.getMessage(), e);
                logger.error("JSON que falló: {}", jsonBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al procesar el JSON: " + e.getMessage(), null, 0L));
            }
            
            // Validar que userId esté presente
            if (review.getUserId() == null) {
                logger.warn("Intento de crear reseña sin userId");
                ApiResponse<Review> response = new ApiResponse<>(
                        false, HttpStatus.BAD_REQUEST.value(), "El ID del usuario (userId) es obligatorio para crear una reseña.", null, 0L);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Validar que productId esté presente
            if (review.getProductId() == null) {
                logger.warn("Intento de crear reseña sin productId");
                ApiResponse<Review> response = new ApiResponse<>(
                        false, HttpStatus.BAD_REQUEST.value(), "El ID del producto (productId) es obligatorio para crear una reseña.", null, 0L);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            Review savedReview = reviewService.save(review);
            logger.info("Reseña guardada exitosamente en la base de datos: ID={}, userId={}, productId={}, rating={}", 
                       savedReview.getId(), savedReview.getUserId(), savedReview.getProductId(), savedReview.getRating());
            logger.info("=== RESEÑA CREADA EXITOSAMENTE - ID: {} ===", savedReview.getId());
            
            ApiResponse<Review> response = new ApiResponse<>(
                    true, HttpStatus.CREATED.value(), "Reseña guardada exitosamente", savedReview, 1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // Error de validación (rating inválido)
            logger.error("Error de validación al guardar reseña: {}", e.getMessage());
            ApiResponse<Review> response = new ApiResponse<>(
                    false, HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // Error general
            logger.error("Error al guardar reseña en la base de datos: {}", e.getMessage(), e);
            ApiResponse<Review> response = new ApiResponse<>(
                    false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al guardar reseña: " + e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Ver reseñas de un componente",
        description = "Obtiene todas las reseñas y calificaciones que los clientes han dejado para un componente específico en PcOneStop. " +
                     "Muestra las calificaciones (1-5 estrellas), comentarios de otros compradores, fecha de la reseña e información del usuario que la dejó. " +
                     "Este endpoint es público, no requiere autenticación. Útil para mostrar las opiniones en la página de detalle del producto " +
                     "y ayudar a otros clientes a evaluar la calidad del componente antes de comprar."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Reseñas obtenidas exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Reseñas obtenidas\", \"data\": [{\"id\": 1, \"productId\": 5, \"userId\": 10, \"rating\": 5, \"comment\": \"Excelente producto\", \"date\": \"2024-01-15\"}], \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Este componente aún no tiene reseñas. Sé el primero en calificarlo.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 204, \"message\": \"Este producto aún no tiene reseñas\", \"data\": null, \"count\": 0}")
            )
        )
    })
    @Parameter(
        name = "productId",
        description = "ID del componente del cual se desean ver las reseñas",
        required = true,
        example = "5"
    )
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

    @Operation(
        summary = "Ver reseñas de un componente (ruta alternativa)",
        description = "Endpoint alternativo para obtener reseñas de un producto usando la estructura de URL '/api/products/{productId}/reviews'. " +
                     "Compatible con la ruta del frontend. Este endpoint es público, no requiere autenticación. " +
                     "Funciona exactamente igual que el endpoint principal pero con una URL diferente para mayor flexibilidad."
    )
    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ApiResponse<List<Review>>> getByProductAlternative(@PathVariable Long productId) {
        return getByProduct(productId);
    }

    @Operation(
        summary = "Crear reseña para un componente específico (ruta alternativa)",
        description = "Endpoint alternativo para crear una reseña usando la estructura de URL '/api/products/{productId}/reviews'. " +
                     "Acepta el productId en la URL y lo asigna automáticamente al review, por lo que no es necesario incluirlo en el body. " +
                     "Compatible con la ruta del frontend. Requiere autenticación JWT. " +
                     "Funciona exactamente igual que el endpoint principal pero con una URL diferente para mayor flexibilidad."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Reseña guardada exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error de validación",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "productId",
        description = "ID del producto para el cual se crea la reseña",
        required = true,
        example = "1"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos de la reseña. El productId se toma automáticamente de la URL, no es necesario incluirlo. " +
                     "Campos obligatorios: userId, rating (1-5). El comment es opcional.",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Review.class),
            examples = {
                @ExampleObject(
                    name = "default",
                    summary = "Reseña con comentario (por defecto)",
                    value = "{\"userId\":10,\"rating\":5,\"comment\":\"Excelente producto, superó mis expectativas\"}"
                ),
                @ExampleObject(
                    name = "Reseña solo rating",
                    summary = "Solo calificación",
                    value = "{\"userId\":10,\"rating\":4}"
                )
            }
        )
    )
    @PostMapping("/{productId}/reviews")
    public ResponseEntity<ApiResponse<Review>> addReviewForProduct(
            @PathVariable Long productId,
            HttpServletRequest request) {
        String jsonBody = null;
        try {
            logger.info("=== INICIO CREAR RESEÑA (RUTA ALTERNATIVA) ===");
            logger.info("productId desde URL: {}", productId);
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Content-Length: {}", request.getContentLength());
            
            // LEER EL BODY DIRECTAMENTE DESDE EL REQUEST
            try (BufferedReader reader = request.getReader()) {
                jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                logger.info("=== JSON BODY RAW (CREAR RESEÑA ALTERNATIVA) ===");
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
            Review review;
            try {
                review = objectMapper.readValue(jsonBody, Review.class);
                logger.info("=== DESERIALIZACIÓN RESEÑA (ALTERNATIVA) ===");
                logger.info("Review deserializado: {}", review);
            } catch (Exception e) {
                logger.error("ERROR al deserializar JSON: {}", e.getMessage(), e);
                logger.error("JSON que falló: {}", jsonBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al procesar el JSON: " + e.getMessage(), null, 0L));
            }
            
            // Asignar el productId desde la URL al review
            review.setProductId(productId);
            logger.info("Creando reseña para producto {}: userId={}, rating={}", productId, review.getUserId(), review.getRating());
            
            // Validar que userId esté presente
            if (review.getUserId() == null) {
                logger.warn("Intento de crear reseña sin userId");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El ID del usuario (userId) es obligatorio para crear una reseña.", null, 0L));
            }
            
            Review savedReview = reviewService.save(review);
            logger.info("Reseña guardada exitosamente: ID={}, userId={}, productId={}, rating={}", 
                       savedReview.getId(), savedReview.getUserId(), savedReview.getProductId(), savedReview.getRating());
            logger.info("=== RESEÑA CREADA EXITOSAMENTE (ALTERNATIVA) - ID: {} ===", savedReview.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, HttpStatus.CREATED.value(), "Reseña guardada exitosamente", savedReview, 1L));
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        } catch (Exception e) {
            logger.error("=== ERROR AL CREAR RESEÑA (ALTERNATIVA) ===", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al guardar la reseña: " + e.getMessage(), null, 0L));
        }
    }
}