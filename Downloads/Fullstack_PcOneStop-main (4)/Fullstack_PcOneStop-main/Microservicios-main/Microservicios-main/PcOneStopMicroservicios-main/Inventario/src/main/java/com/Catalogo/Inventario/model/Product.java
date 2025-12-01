package com.Catalogo.Inventario.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Componente de PC en el catálogo de PcOneStop. Para crear un producto, los campos obligatorios son: name, brand, model, category, price, stock. " +
                     "Los campos de oferta (isOnSale, discount, offerStartDate, offerEndDate) son opcionales. " +
                     "También se puede enviar un objeto 'offer' con {discount, startDate, endDate} que se mapeará automáticamente.")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autogenerado (no se envía en el request)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Schema(description = "Nombre completo del producto (obligatorio)", example = "GeForce RTX 4070")
    private String name;

    @NotBlank(message = "La marca es obligatoria")
    @Schema(description = "Marca del componente (obligatorio). Ejemplos: MSI, ASUS, NVIDIA, AMD, Intel", example = "MSI")
    private String brand;

    @NotBlank(message = "El modelo es obligatorio")
    @Schema(description = "Modelo específico del componente (obligatorio)", example = "Ventus 3X OC")
    private String model;

    @NotBlank(message = "La categoría es obligatoria")
    @Schema(description = "Categoría del componente (obligatorio). Ejemplos: GPU, CPU, RAM, SSD, HDD, Motherboard, PSU", example = "GPU", allowableValues = {"GPU", "CPU", "RAM", "SSD", "HDD", "Motherboard", "PSU", "Case", "Cooler", "Peripheral"})
    private String category;

    @Column(nullable = false)
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Schema(description = "Precio unitario en pesos (obligatorio, debe ser mayor o igual a 0)", example = "699.99", minimum = "0")
    private Double price;

    @Column(nullable = false)
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Schema(description = "Cantidad disponible en inventario (obligatorio, debe ser mayor o igual a 0)", example = "10", minimum = "0")
    private Integer stock;

    @Column(length = 1000)
    @Schema(description = "Descripción detallada del producto (opcional, máximo 1000 caracteres)", example = "Tarjeta gráfica de alto rendimiento para gaming, ideal para juegos en 1440p y 4K. Incluye 12GB de VRAM GDDR6X.")
    private String description;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "URL de la imagen del producto o imagen en base64 (opcional). Si es base64, debe comenzar con 'data:image/'.", example = "https://example.com/images/rtx4070.jpg")
    private String image;

    @Column(nullable = false)
    @Schema(description = "Indica si el producto está actualmente en oferta (opcional, por defecto false)", example = "false", defaultValue = "false")
    private Boolean isOnSale = false;

    @Column
    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Max(value = 100, message = "El descuento no puede ser mayor a 100%")
    @Schema(description = "Porcentaje de descuento si está en oferta (opcional, rango 0-100)", example = "15", minimum = "0", maximum = "100")
    private Integer discount = 0;

    @Column
    @Schema(description = "Fecha de inicio de la oferta en formato YYYY-MM-DD (opcional)", example = "2024-01-01")
    private String offerStartDate;

    @Column
    @Schema(description = "Fecha de fin de la oferta en formato YYYY-MM-DD (opcional)", example = "2024-12-31")
    private String offerEndDate;

    // Setter personalizado para manejar el objeto 'offer' del frontend
    @JsonProperty("offer")
    public void setOffer(Map<String, Object> offer) {
        if (offer != null) {
            Object discountObj = offer.get("discount");
            if (discountObj != null) {
                if (discountObj instanceof Number) {
                    this.discount = ((Number) discountObj).intValue();
                } else if (discountObj instanceof String) {
                    try {
                        this.discount = Integer.parseInt((String) discountObj);
                    } catch (NumberFormatException e) {
                        this.discount = 0;
                    }
                }
            }
            
            Object startDateObj = offer.get("startDate");
            if (startDateObj != null) {
                this.offerStartDate = startDateObj.toString();
            }
            
            Object endDateObj = offer.get("endDate");
            if (endDateObj != null) {
                this.offerEndDate = endDateObj.toString();
            }
        }
    }
}