package com.Catalogo.Inventario.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa las categorías de productos")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la categoría", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    @Schema(description = "Nombre de la categoría", example = "GPU")
    private String name;

    @Column(length = 500)
    @Schema(description = "Descripción de la categoría", example = "Tarjetas gráficas para gaming y trabajo profesional")
    private String description;

    @Column(length = 255)
    @Schema(description = "URL del ícono de la categoría", example = "https://example.com/icons/gpu.png")
    private String iconUrl;
}

