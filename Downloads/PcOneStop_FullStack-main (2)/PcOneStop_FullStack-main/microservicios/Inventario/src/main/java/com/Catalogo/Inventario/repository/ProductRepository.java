package com.Catalogo.Inventario.repository;

import com.Catalogo.Inventario.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    // Buscar productos por categoría
    List<Product> findByCategory(String category);
    
    // Buscar productos en oferta
    List<Product> findByIsOnSaleTrue();
    
    // Buscar productos por marca
    List<Product> findByBrand(String brand);
}
