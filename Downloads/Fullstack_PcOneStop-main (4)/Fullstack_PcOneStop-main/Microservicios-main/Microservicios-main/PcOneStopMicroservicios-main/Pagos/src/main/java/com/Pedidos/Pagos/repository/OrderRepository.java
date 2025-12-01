package com.Pedidos.Pagos.repository;

import com.Pedidos.Pagos.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Buscar todos los pedidos de un usuario específico
    List<Order> findByUserId(Long userId);
}