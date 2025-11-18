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
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la reseña (autogenerado)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "ID del producto reseñado", example = "5")
    private Long productId;

    @Column(nullable = false)
    @Schema(description = "ID del usuario que escribe la reseña", example = "10")
    private Long userId;

    @Column(nullable = false)
    @Schema(description = "Calificación de 1 a 5 estrellas", example = "5")
    private Integer rating;

    @Schema(description = "Comentario u opinión", example = "Excelente producto, llegó a tiempo.")
    private String comment;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate date = LocalDate.now();
}