package com.Catalogo.Inventario.model;

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
public class ProductReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productId; // El producto reportado

    @Column(nullable = false)
    private Long userId;    // Quién lo reportó

    private String reason;  // "Es falso", "Está roto", etc.

    private LocalDate date = LocalDate.now();
}