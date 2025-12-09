package com.Catalogo.Inventario.repository;

import com.Catalogo.Inventario.model.ProductReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ProductReport, Long> {
    List<ProductReport> findByProductId(Long productId);
    long countByProductId(Long productId);

    // --- NUEVO: Borrar reportes por ID de producto ---
    void deleteByProductId(Long productId);
}