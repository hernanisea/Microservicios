package com.Catalogo.Inventario.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "product_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de un componente con problemas en PcOneStop. El productId se toma de la URL automáticamente.")
public class ProductReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del reporte (autogenerado, no se envía en el request)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "ID del producto reportado (se toma de la URL automáticamente, no es necesario enviarlo)", example = "5", accessMode = Schema.AccessMode.READ_ONLY)
    private Long productId;

    @Column(nullable = false)
    @Schema(description = "ID del usuario que realiza el reporte (obligatorio). Se puede extraer del token JWT o enviarse en el body.", example = "10")
    private Long userId;

    @Schema(description = "Motivo del reporte (opcional pero recomendado). Ejemplos: 'Precio incorrecto', 'Información falsa', 'Imagen incorrecta', 'Producto defectuoso'", example = "Precio incorrecto en el sitio web")
    private String reason;

    @Schema(description = "Fecha del reporte (se asigna automáticamente, no se envía en el request)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate date = LocalDate.now();
}