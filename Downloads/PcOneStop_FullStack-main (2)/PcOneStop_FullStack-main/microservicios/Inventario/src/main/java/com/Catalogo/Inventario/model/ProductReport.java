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
    private String productId; // El producto reportado (ahora es String)

    @Column(nullable = false)
    private String userId;    // Quién lo reportó (ahora es String)

    private String reason;  // "Es falso", "Está roto", etc.

    private LocalDate date = LocalDate.now();
}
