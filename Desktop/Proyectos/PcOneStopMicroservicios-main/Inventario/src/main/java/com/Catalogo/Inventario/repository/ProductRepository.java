package com.Catalogo.Inventario.repository;

import com.Catalogo.Inventario.model.Category;
import com.Catalogo.Inventario.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Busca productos por vendedor.
     */
    List<Product> findBySellerId(Long sellerId);

    /**
     * Busca productos por categoría.
     */
    List<Product> findByCategoryEntity(Category category);

    /**
     * Busca productos por nombre (parcial, case-insensitive).
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Busca productos por marca.
     */
    List<Product> findByBrand(String brand);
}
