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
    @Schema(description = "ID único del producto (UUID o slug)", example = "cpu-ryzen-5600")
    private String id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Schema(description = "Nombre del producto", example = "AMD Ryzen 5 5600")
    private String name;

    @NotBlank(message = "La categoría es obligatoria")
    @Schema(description = "Categoría", example = "CPU")
    private String category;

    @NotBlank(message = "La marca es obligatoria")
    @Schema(description = "Marca", example = "AMD")
    private String brand;

    @Column(nullable = false)
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Schema(description = "Precio del producto", example = "129990")
    private Double price;

    @Column(nullable = false)
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Stock disponible", example = "20")
    private Integer stock;

    @Column(length = 500)
    @Schema(description = "URL de la imagen del producto", example = "https://example.com/image.png")
    private String image;

    @Column(length = 1000)
    @Schema(description = "Descripción del producto", example = "6C/12T, gran rendimiento precio-rendimiento.")
    private String description;

    @Column(nullable = false)
    @Schema(description = "Indica si el producto está en oferta", example = "true")
    private Boolean isOnSale = false;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "discount", column = @Column(name = "offer_discount")),
        @AttributeOverride(name = "startDate", column = @Column(name = "offer_start_date")),
        @AttributeOverride(name = "endDate", column = @Column(name = "offer_end_date"))
    })
    @Schema(description = "Detalles de la oferta (si aplica)")
    private Offer offer;
}
