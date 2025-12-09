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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    // Tests createOrder() 
    @Test
    public void testCreateOrder_EstadoInicialPendiente() {
        // DADO: un pedido sin estado
        Order pedido = new Order(null, 1L, 500.0, null, "1,2,3", LocalDateTime.now(), 5L);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: creamos el pedido
        Order resultado = orderService.createOrder(pedido);

        // ENTONCES: el estado es PENDIENTE
        assertEquals("PENDIENTE", resultado.getStatus());
        verify(orderRepository).save(pedido);
    }

    @Test
    public void testCreateOrder_SobreescribeEstadoSiVieneDiferente() {
        // DADO: un pedido con estado "COMPLETADO" (intento de manipulación)
        Order pedido = new Order(null, 1L, 500.0, "COMPLETADO", "1,2", LocalDateTime.now(), 5L);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: creamos el pedido
        Order resultado = orderService.createOrder(pedido);

        // ENTONCES: siempre es PENDIENTE (se ignora el valor enviado)
        assertEquals("PENDIENTE", resultado.getStatus());
    }

    // Tests findByUserId() 
    @Test
    public void testFindByUserId_RetornaPedidosDelUsuario() {
        // DADO: 2 pedidos del usuario 1
        Order o1 = new Order(1L, 1L, 100.0, "PENDIENTE", "1", LocalDateTime.now(), 5L);
        Order o2 = new Order(2L, 1L, 200.0, "ENVIADO", "2", LocalDateTime.now(), 5L);
        when(orderRepository.findByUserId(1L)).thenReturn(Arrays.asList(o1, o2));

        // CUANDO: buscamos por userId
        List<Order> resultado = orderService.findByUserId(1L);

        // ENTONCES: retorna 2 pedidos
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindByUserId_UsuarioSinPedidos() {
        // DADO: usuario sin pedidos
        when(orderRepository.findByUserId(999L)).thenReturn(Arrays.asList());

        // CUANDO: buscamos por userId
        List<Order> resultado = orderService.findByUserId(999L);

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // Tests findById() 
    @Test
    public void testFindById_PedidoExiste() {
        // DADO: un pedido con ID 1
        Order pedido = new Order(1L, 1L, 300.0, "PENDIENTE", "1,2", LocalDateTime.now(), 5L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pedido));

        // CUANDO: buscamos por ID
        Order resultado = orderService.findById(1L);

        // ENTONCES: retorna el pedido
        assertEquals(1L, resultado.getId());
        assertEquals(300.0, resultado.getTotalAmount());
    }

    @Test
    public void testFindById_PedidoNoExiste_LanzaExcepcion() {
        // DADO: un ID que no existe
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            orderService.findById(999L);
        });
        assertTrue(ex.getMessage().contains("Pedido no encontrado"));
    }

    // Tests findBySellerId() 
    @Test
    public void testFindBySellerId_RetornaPedidosDelVendedor() {
        // DADO: 2 pedidos del vendedor 5
        Order o1 = new Order(1L, 10L, 100.0, "PENDIENTE", "1", LocalDateTime.now(), 5L);
        Order o2 = new Order(2L, 20L, 200.0, "ENVIADO", "2", LocalDateTime.now(), 5L);
        when(orderRepository.findBySellerId(5L)).thenReturn(Arrays.asList(o1, o2));

        // CUANDO: buscamos por sellerId
        List<Order> resultado = orderService.findBySellerId(5L);

        // ENTONCES: retorna 2 pedidos
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindBySellerId_VendedorSinPedidos() {
        // DADO: vendedor sin pedidos
        when(orderRepository.findBySellerId(999L)).thenReturn(Arrays.asList());

        // CUANDO: buscamos por sellerId
        List<Order> resultado = orderService.findBySellerId(999L);

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // Tests updateStatus() 
    @Test
    public void testUpdateStatus_CambiaEstadoCorrectamente() {
        // DADO: un pedido en estado PENDIENTE
        Order pedido = new Order(1L, 1L, 100.0, "PENDIENTE", "1", LocalDateTime.now(), 5L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: actualizamos a ENVIADO
        Order resultado = orderService.updateStatus(1L, "ENVIADO");

        // ENTONCES: el estado cambia
        assertEquals("ENVIADO", resultado.getStatus());
        verify(orderRepository).save(pedido);
    }

    @Test
    public void testUpdateStatus_CambiarACompletado() {
        // DADO: un pedido en estado ENVIADO
        Order pedido = new Order(1L, 1L, 100.0, "ENVIADO", "1", LocalDateTime.now(), 5L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: actualizamos a COMPLETADO
        Order resultado = orderService.updateStatus(1L, "COMPLETADO");

        // ENTONCES: el estado cambia
        assertEquals("COMPLETADO", resultado.getStatus());
    }

    // Tests findAll() 
    @Test
    public void testFindAll_RetornaTodosLosPedidos() {
        // DADO: 3 pedidos en la BD
        Order o1 = new Order(1L, 1L, 100.0, "PENDIENTE", "1", LocalDateTime.now(), 2L);
        Order o2 = new Order(2L, 2L, 200.0, "COMPLETADO", "2,3", LocalDateTime.now(), 3L);
        Order o3 = new Order(3L, 1L, 150.0, "EN_CAMINO", "4", LocalDateTime.now(), 2L);
        when(orderRepository.findAll()).thenReturn(Arrays.asList(o1, o2, o3));

        // CUANDO: obtenemos todos
        List<Order> resultado = orderService.findAll();

        // ENTONCES: retorna 3 pedidos
        assertEquals(3, resultado.size());
    }

    @Test
    public void testFindAll_SinPedidos() {
        // DADO: no hay pedidos
        when(orderRepository.findAll()).thenReturn(Arrays.asList());

        // CUANDO: obtenemos todos
        List<Order> resultado = orderService.findAll();

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // Tests deleteById() 
    @Test
    public void testDeleteById_EliminaPedidoCorrectamente() {
        // DADO: un pedido que existe
        doNothing().when(orderRepository).deleteById(1L);

        // CUANDO: eliminamos el pedido
        orderService.deleteById(1L);

        // ENTONCES: se llama al repositorio para eliminar
        verify(orderRepository).deleteById(1L);
    }

    @Test
    public void testDeleteById_NoLanzaExcepcionSiNoExiste() {
        // DADO: un ID que no existe
        doNothing().when(orderRepository).deleteById(999L);

        // CUANDO: intentamos eliminar (el servicio no valida existencia)
        orderService.deleteById(999L);

        // ENTONCES: no lanza excepción (solo delega al repositorio)
        verify(orderRepository).deleteById(999L);
    }
}
