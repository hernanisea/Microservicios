package com.Pedidos.Pagos.repository;

import com.Pedidos.Pagos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    // Buscar pedidos por email del cliente
    List<Order> findByCustomerEmail(String customerEmail);
    
    // Buscar pedidos ordenados por fecha de creación
    List<Order> findAllByOrderByCreatedAtDesc();
}
