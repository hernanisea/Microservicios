package com.Catalogo.Inventario.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autogenerado", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Schema(description = "Nombre del producto", example = "GeForce RTX 4070")
    private String name;

    @NotBlank(message = "La marca es obligatoria")
    @Schema(description = "Marca", example = "MSI")
    private String brand;

    @NotBlank(message = "El modelo es obligatorio")
    @Schema(description = "Modelo", example = "Ventus 3X")
    private String model;

    @NotBlank(message = "La categoría es obligatoria")
    @Schema(description = "Categoría", example = "GPU")
    private String category;

    @Column(nullable = false)
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Schema(description = "Precio unitario", example = "699.99")
    private Double price;

    @Column(nullable = false)
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Stock disponible", example = "10")
    private Integer stock;

    @Column(nullable = false)
    @NotNull(message = "El ID del vendedor es obligatorio")
    @Schema(description = "ID del vendedor dueño del producto", example = "5")
    private Long sellerId;
}