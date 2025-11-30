package com.Catalogo.Inventario.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Offer {
    private Integer discount;      // Porcentaje de descuento (ej: 20 = 20%)
    private String startDate;      // Fecha inicio (ISO 8601)
    private String endDate;        // Fecha fin (ISO 8601)
}

