package com.Catalogo.Inventario.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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
    @Schema(description = "Nombre del producto", example = "GeForce RTX 4070")
    private String name;

    @Schema(description = "Marca", example = "MSI")
    private String brand;

    @Schema(description = "Modelo", example = "Ventus 3X")
    private String model;

    @Schema(description = "Categoría", example = "GPU")
    private String category;

    @Column(nullable = false)
    @Schema(description = "Precio unitario", example = "699.99")
    private Double price;

    @Column(nullable = false)
    @Schema(description = "Stock disponible", example = "10")
    private Integer stock;

    @Schema(description = "ID del vendedor dueño del producto", example = "5")
    private Long sellerId;
}