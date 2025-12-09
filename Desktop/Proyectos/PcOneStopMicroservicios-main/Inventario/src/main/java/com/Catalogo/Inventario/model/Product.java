package com.Catalogo.Inventario.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@Schema(description = "Entidad que representa un producto del catálogo")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autogenerado del producto", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Schema(description = "Nombre del producto", example = "GeForce RTX 4070", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "La marca es obligatoria")
    @Schema(description = "Marca del producto", example = "MSI", requiredMode = Schema.RequiredMode.REQUIRED)
    private String brand;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El modelo es obligatorio")
    @Schema(description = "Modelo del producto", example = "Ventus 3X", requiredMode = Schema.RequiredMode.REQUIRED)
    private String model;

    // --- RELACIÓN CON CATEGORY (FK normalizada) ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @Schema(description = "Categoría del producto")
    private Category categoryEntity;

    // Campo transitorio para compatibilidad con el frontend
    @Transient
    @JsonProperty("category")
    @Schema(description = "Nombre de la categoría", example = "GPU")
    private String category;

    @Column(nullable = false)
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Schema(description = "Precio unitario en soles", example = "2599.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Column(nullable = false)
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad disponible en inventario", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer stock;

    @Column(nullable = false)
    @NotNull(message = "El ID del vendedor es obligatorio")
    @Schema(description = "ID del vendedor dueño del producto", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sellerId;

    @Column(length = 1000)
    @Schema(description = "Descripción detallada del producto", example = "Tarjeta gráfica de última generación con 12GB GDDR6X")
    private String description;

    @Column(length = 500)
    @Schema(description = "URL de la imagen del producto", example = "https://example.com/images/rtx4070.jpg")
    private String imageUrl;

    // Getter para serialización JSON
    @JsonProperty("category")
    public String getCategory() {
        return categoryEntity != null ? categoryEntity.getName() : category;
    }

    // Setter para deserialización desde el frontend
    public void setCategory(String category) {
        this.category = category;
    }
}
