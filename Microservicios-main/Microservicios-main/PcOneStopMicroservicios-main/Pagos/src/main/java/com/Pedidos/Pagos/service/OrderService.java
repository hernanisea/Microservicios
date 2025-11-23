package com.Pedidos.Pagos.service;

import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Order order) {
        // Lógica de negocio: Al crear, el estado inicial siempre es PENDIENTE
        order.setStatus("PENDIENTE");
        return orderRepository.save(order);
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public List<Order> findBySellerId(Long sellerId) {
        return orderRepository.findBySellerId(sellerId);
    }

    public Order updateStatus(Long id, String newStatus) {
        Order order = findById(id); // Busca el pedido
        order.setStatus(newStatus); // Cambia el estado
        return orderRepository.save(order); // Guarda en MySQL
    }
}