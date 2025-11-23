package com.Pedidos.Pagos.model;

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
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del pedido autogenerado", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(description = "ID del usuario que realiza la compra", example = "10")
    private Long userId;

    @Column(nullable = false)
    @Schema(description = "Monto total de la compra", example = "15000.00")
    private Double totalAmount;

    @Column(nullable = false)
    @Schema(description = "Estado del pedido", example = "PENDIENTE")
    private String status;

    @Schema(description = "Lista de IDs de productos (formato CSV o JSON simple)", example = "1,5,8")
    private String productIds;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt = LocalDateTime.now();
     
    @Column(nullable = false)
    private Long sellerId;
}