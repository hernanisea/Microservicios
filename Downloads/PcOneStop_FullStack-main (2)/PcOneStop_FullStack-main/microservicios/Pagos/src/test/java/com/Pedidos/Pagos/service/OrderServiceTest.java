package com.Pedidos.Pagos.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.model.OrderItem;
import com.Pedidos.Pagos.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    // Helper para crear pedidos de prueba
    private Order crearPedido(String id, String customerEmail, String customerName, Double total) {
        Order order = new Order();
        order.setId(id);
        order.setCustomerEmail(customerEmail);
        order.setCustomerName(customerName);
        order.setCustomerLastName("Test");
        order.setTotal(total);
        order.setShippingStreet("Calle Test 123");
        order.setShippingRegion("Región Test");
        order.setShippingComuna("Comuna Test");
        order.setItems(Arrays.asList(
            new OrderItem("prod-1", "Producto 1", 100.0, 1)
        ));
        return order;
    }

    // ==================== TESTS PARA createOrder() ====================

    @Test
    public void testCreateOrder_GeneraIdSiNoViene() {
        // DADO: un pedido sin ID
        Order pedido = crearPedido(null, "cliente@test.com", "Juan", 500.0);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: creamos el pedido
        Order resultado = orderService.createOrder(pedido);

        // ENTONCES: se genera un ID automáticamente
        assertNotNull(resultado.getId());
        verify(orderRepository).save(pedido);
    }

    @Test
    public void testCreateOrder_GeneraFechaCreacion() {
        // DADO: un pedido sin fecha de creación
        Order pedido = crearPedido(null, "cliente@test.com", "Juan", 500.0);
        pedido.setCreatedAt(null);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: creamos el pedido
        Order resultado = orderService.createOrder(pedido);

        // ENTONCES: se genera la fecha automáticamente
        assertNotNull(resultado.getCreatedAt());
    }

    @Test
    public void testCreateOrder_ConItemsMultiples() {
        // DADO: un pedido con múltiples items
        Order pedido = crearPedido(null, "cliente@test.com", "Juan", 750.0);
        pedido.setItems(Arrays.asList(
            new OrderItem("cpu-ryzen-5600", "AMD Ryzen 5 5600", 130.0, 1),
            new OrderItem("gpu-rtx-4060", "RTX 4060", 350.0, 1),
            new OrderItem("ram-ddr5-16", "DDR5 16GB", 70.0, 2)
        ));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: creamos el pedido
        Order resultado = orderService.createOrder(pedido);

        // ENTONCES: el pedido tiene 3 items
        assertEquals(3, resultado.getItems().size());
    }

    // ==================== TESTS PARA findAll() ====================

    @Test
    public void testFindAll_RetornaPedidosOrdenadosPorFecha() {
        // DADO: 2 pedidos
        Order o1 = crearPedido("20250101001", "cliente1@test.com", "Juan", 100.0);
        o1.setCreatedAt("2025-01-01T10:00:00");
        Order o2 = crearPedido("20250102001", "cliente2@test.com", "Pedro", 200.0);
        o2.setCreatedAt("2025-01-02T10:00:00");
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList(o2, o1));

        // CUANDO: obtenemos todos
        List<Order> resultado = orderService.findAll();

        // ENTONCES: retorna 2 pedidos
        assertEquals(2, resultado.size());
        assertEquals("20250102001", resultado.get(0).getId()); // Más reciente primero
    }

    @Test
    public void testFindAll_ListaVacia() {
        // DADO: no hay pedidos
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Arrays.asList());

        // CUANDO: obtenemos todos
        List<Order> resultado = orderService.findAll();

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS PARA findById() ====================

    @Test
    public void testFindById_PedidoExiste() {
        // DADO: un pedido con ID específico
        Order pedido = crearPedido("20250115001", "cliente@test.com", "Ana", 300.0);
        when(orderRepository.findById("20250115001")).thenReturn(Optional.of(pedido));

        // CUANDO: buscamos por ID
        Order resultado = orderService.findById("20250115001");

        // ENTONCES: retorna el pedido
        assertEquals("20250115001", resultado.getId());
        assertEquals("cliente@test.com", resultado.getCustomerEmail());
        assertEquals(300.0, resultado.getTotal());
    }

    @Test
    public void testFindById_PedidoNoExiste_LanzaExcepcion() {
        // DADO: un ID que no existe
        when(orderRepository.findById("pedido-inexistente")).thenReturn(Optional.empty());

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            orderService.findById("pedido-inexistente");
        });
        assertTrue(ex.getMessage().contains("Pedido no encontrado"));
    }

    // ==================== TESTS PARA findByCustomerEmail() ====================

    @Test
    public void testFindByCustomerEmail_RetornaPedidosDelCliente() {
        // DADO: 2 pedidos del mismo cliente
        Order o1 = crearPedido("20250101001", "cliente@test.com", "Juan", 100.0);
        Order o2 = crearPedido("20250102001", "cliente@test.com", "Juan", 200.0);
        when(orderRepository.findByCustomerEmail("cliente@test.com")).thenReturn(Arrays.asList(o1, o2));

        // CUANDO: buscamos por email
        List<Order> resultado = orderService.findByCustomerEmail("cliente@test.com");

        // ENTONCES: retorna 2 pedidos
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindByCustomerEmail_ClienteSinPedidos() {
        // DADO: cliente sin pedidos
        when(orderRepository.findByCustomerEmail("nuevo@test.com")).thenReturn(Arrays.asList());

        // CUANDO: buscamos por email
        List<Order> resultado = orderService.findByCustomerEmail("nuevo@test.com");

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS PARA deleteOrder() ====================

    @Test
    public void testDeleteOrder_EliminaCorrectamente() {
        // CUANDO: eliminamos pedido
        orderService.deleteOrder("20250101001");

        // ENTONCES: se llama al repositorio
        verify(orderRepository).deleteById("20250101001");
    }

    // ==================== TESTS PARA datos de envío ====================

    @Test
    public void testCreateOrder_ConDatosDeEnvioCompletos() {
        // DADO: un pedido con todos los datos de envío
        Order pedido = crearPedido(null, "cliente@test.com", "María", 500.0);
        pedido.setShippingStreet("Av. Principal 456");
        pedido.setShippingDepartment("Depto 1001");
        pedido.setShippingRegion("Región Metropolitana");
        pedido.setShippingComuna("Las Condes");
        pedido.setShippingIndications("Portería principal, timbre 10");
        
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: creamos el pedido
        Order resultado = orderService.createOrder(pedido);

        // ENTONCES: los datos de envío se guardan correctamente
        assertEquals("Av. Principal 456", resultado.getShippingStreet());
        assertEquals("Depto 1001", resultado.getShippingDepartment());
        assertEquals("Las Condes", resultado.getShippingComuna());
        assertNotNull(resultado.getShippingIndications());
    }
}
