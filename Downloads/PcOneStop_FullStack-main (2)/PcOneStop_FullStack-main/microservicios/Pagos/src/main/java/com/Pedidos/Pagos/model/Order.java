package com.Pedidos.Pagos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @Schema(description = "ID del pedido (formato fecha o UUID)", example = "20240705")
    private String id;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Schema(description = "Lista de items del pedido")
    private List<OrderItem> items;

    @Column(nullable = false)
    @Schema(description = "Total del pedido", example = "129990")
    private Double total;

    @Column(nullable = false)
    @Schema(description = "Fecha de creación (ISO 8601)")
    private String createdAt;

    // Datos del cliente
    @Column(nullable = false)
    @Schema(description = "Email del cliente", example = "cliente@gmail.com")
    private String customerEmail;

    @Column(nullable = false)
    @Schema(description = "Nombre del cliente", example = "Pedro")
    private String customerName;

    @Column
    @Schema(description = "Apellido del cliente", example = "Pérez")
    private String customerLastName;

    // Datos de envío
    @Column(nullable = false)
    @Schema(description = "Calle de envío", example = "Calle Falsa 123")
    private String shippingStreet;

    @Column
    @Schema(description = "Departamento (opcional)", example = "Depto 603")
    private String shippingDepartment;

    @Column(nullable = false)
    @Schema(description = "Región", example = "Región Metropolitana")
    private String shippingRegion;

    @Column(nullable = false)
    @Schema(description = "Comuna", example = "Cerrillos")
    private String shippingComuna;

    @Column
    @Schema(description = "Indicaciones adicionales", example = "Dejar en conserjería")
    private String shippingIndications;

    // Método para generar ID basado en fecha si no viene
    @PrePersist
    public void prePersist() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        }
        if (this.createdAt == null || this.createdAt.isEmpty()) {
            this.createdAt = LocalDateTime.now().toString();
        }
    }
}
