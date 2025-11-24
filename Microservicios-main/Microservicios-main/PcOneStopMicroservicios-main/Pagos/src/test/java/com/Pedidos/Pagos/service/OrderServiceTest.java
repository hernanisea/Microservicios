package com.Pedidos.Pagos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    public void testCreateOrderSetsStatusPending() {
        // Datos de prueba (Incluyendo sellerId al final)
        Order newOrder = new Order(null, 1L, 100.0, null, "1,2", LocalDateTime.now(), 5L);
        
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order saved = orderService.createOrder(newOrder);

        // Debe forzar PENDIENTE
        assertEquals("PENDIENTE", saved.getStatus());
        verify(orderRepository).save(newOrder);
    }

    @Test
    public void testUpdateStatus() {
        // Datos de prueba (Incluyendo sellerId al final)
        Order existing = new Order(1L, 1L, 100.0, "PENDIENTE", "1", LocalDateTime.now(), 5L);
        
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order updated = orderService.updateStatus(1L, "ENVIADO");

        assertEquals("ENVIADO", updated.getStatus());
    }
}