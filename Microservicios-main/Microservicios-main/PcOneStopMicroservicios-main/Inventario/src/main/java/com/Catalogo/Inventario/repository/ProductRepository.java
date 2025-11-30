package com.Catalogo.Inventario.repository;

import com.Catalogo.Inventario.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Aquí puedes agregar métodos custom si necesitas, ej:
    // List<Product> findBySellerId(Long sellerId);
}