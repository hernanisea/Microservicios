package com.Catalogo.Inventario.repository;

import com.Catalogo.Inventario.model.ProductReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<ProductReport, Long> {
    List<ProductReport> findByProductId(String productId);
    long countByProductId(String productId);
    void deleteByProductId(String productId);
}
