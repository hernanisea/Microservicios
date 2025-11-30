package com.Resenas.Calificaciones.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @Schema(description = "ID de la reseña (UUID)", example = "review-abc123")
    private String id;

    @Column(nullable = false)
    @Schema(description = "ID del producto reseñado", example = "cpu-ryzen-5600")
    private String productId;

    @Column(nullable = true)
    @Schema(description = "ID del usuario (null si es anónimo)", example = "user-client-01")
    private String userId;

    @Column(nullable = false)
    @Schema(description = "Nombre visible del autor", example = "Pedro")
    private String author;

    @Column(nullable = false)
    @Min(1)
    @Max(5)
    @Schema(description = "Calificación de 1 a 5 estrellas", example = "5")
    private Integer rating;

    @Column(length = 1000)
    @Schema(description = "Comentario u opinión", example = "Excelente producto, llegó a tiempo.")
    private String comment;

    @Column(nullable = false)
    @Schema(description = "Fecha de la reseña (ISO string)")
    private String date;
}
