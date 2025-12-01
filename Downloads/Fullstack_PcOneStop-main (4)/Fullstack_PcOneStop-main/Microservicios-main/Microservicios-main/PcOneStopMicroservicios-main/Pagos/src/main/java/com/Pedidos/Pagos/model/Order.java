package com.Pedidos.Pagos.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders") // "order" es una palabra reservada en SQL, usamos "orders"
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos desconocidos en el JSON
@Schema(description = "Pedido de compra de componentes PC en PcOneStop. El userId se extrae automáticamente del token JWT si no se proporciona.")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del pedido autogenerado (no se envía en el request)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "ID del usuario que realiza la compra. Se extrae automáticamente del token JWT si no se proporciona. Opcional en el request.", example = "10")
    private Long userId;

    @Column(nullable = false)
    @JsonAlias({"total"}) // Acepta 'total' además de 'totalAmount'
    @Schema(description = "Monto total de la compra en pesos. Puede enviarse como 'total' o 'totalAmount'. Debe ser mayor a cero. Obligatorio.", example = "15999.99", minimum = "0.01")
    private Double totalAmount;

    @Column(nullable = false)
    @Schema(description = "Estado del pedido. Se asigna automáticamente como PENDIENTE (no se envía en el request)", example = "PENDIENTE", accessMode = Schema.AccessMode.READ_ONLY, allowableValues = {"PENDIENTE", "EN_PROCESO", "ENVIADO", "COMPLETADO", "CANCELADO"})
    private String status;

    @Schema(description = "Lista de IDs de productos comprados separados por comas. Formato: '1,5,8' o '1,2,3'. Opcional pero recomendado para referencia.", example = "1,5,8")
    private String productIds;

    @Schema(description = "Fecha y hora de creación del pedido (se asigna automáticamente, no se envía en el request)", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt = LocalDateTime.now();
}