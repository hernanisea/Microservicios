package com.Pedidos.Pagos;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.repository.OrderRepository;
import com.Pedidos.Pagos.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Test
    public void testCreateOrderSetsStatusToPending() {
        // 1. Datos de prueba (Simulamos un pedido que llega sin estado o con otro estado)
        Order inputOrder = new Order(null, 10L, 25000.0, null, "1,2", LocalDateTime.now());
        
        // 2. Configurar Mock: cuando guarde, devuelve el mismo objeto
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // 3. Ejecutar servicio
        Order createdOrder = orderService.createOrder(inputOrder);

        // 4. Verificar que el servicio forzó el estado a "PENDIENTE"
        assertNotNull(createdOrder);
        assertEquals("PENDIENTE", createdOrder.getStatus());
        assertEquals(25000.0, createdOrder.getTotalAmount());
        
        // Verificar que se llamó al repositorio una vez
        verify(orderRepository, times(1)).save(any(Order.class));
    }
}