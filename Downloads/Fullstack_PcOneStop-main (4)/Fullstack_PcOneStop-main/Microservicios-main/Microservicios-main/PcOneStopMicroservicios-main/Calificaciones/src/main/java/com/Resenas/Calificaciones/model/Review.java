package com.Resenas.Calificaciones.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reseña y calificación de un componente PC en PcOneStop. Los campos obligatorios son: productId, userId, rating. El comment es opcional.")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la reseña (autogenerado, no se envía en el request)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    @Schema(description = "ID del componente PC que se está reseñando (obligatorio). Si se usa la ruta /api/products/{productId}/reviews, se toma de la URL automáticamente.", example = "5")
    private Long productId;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "ID del usuario que escribe la reseña (obligatorio). Se puede extraer del token JWT o enviarse en el body.", example = "10")
    private Long userId;

    @Column(nullable = false)
    @Schema(description = "Calificación del producto de 1 a 5 estrellas (obligatorio). 1=Muy malo, 2=Malo, 3=Regular, 4=Bueno, 5=Excelente", example = "5", minimum = "1", maximum = "5")
    private Integer rating;

    @Column(length = 1000)
    @Schema(description = "Comentario u opinión sobre el producto (opcional, máximo 1000 caracteres). Puede incluir pros, contras, experiencia de uso y recomendaciones.", example = "Excelente GPU, muy buena para gaming en 1440p. Llegó a tiempo y en perfecto estado. La recomiendo totalmente.")
    private String comment;

    @Column(nullable = false)
    @Schema(description = "Fecha de la reseña (se asigna automáticamente, no se envía en el request)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate date = LocalDate.now();
}