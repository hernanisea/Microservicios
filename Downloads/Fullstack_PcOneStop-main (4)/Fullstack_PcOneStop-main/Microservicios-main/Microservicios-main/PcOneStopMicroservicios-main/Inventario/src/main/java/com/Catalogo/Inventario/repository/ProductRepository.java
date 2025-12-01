package com.Catalogo.Inventario.repository;

import com.Catalogo.Inventario.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Buscar productos que están en oferta
    List<Product> findByIsOnSaleTrue();
}