package com.Pedidos.Pagos.config;

import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * DataLoader para precargar datos iniciales en la base de datos de Pedidos.
 * Se ejecuta automáticamente al iniciar la aplicación.
 * 
 * Datos precargados:
 * - Pedidos de ejemplo en diferentes estados
 * 
 * IDs de referencia:
 * - Clientes: Juan (4), Ana (5), Luis (6)
 * - Vendedores: María TechStore (2), Pedro PCMaster (3)
 * - Productos: IDs del 1 al 14
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        if (orderRepository.count() == 0) {
            loadOrders();
        }
        
        System.out.println("[Pagos] Datos precargados correctamente");
        System.out.println("   Pedidos: " + orderRepository.count());
    }

    private void loadOrders() {
        System.out.println("Cargando pedidos de prueba...");

        // ===== PEDIDOS DEL CLIENTE JUAN (ID: 4) =====
        
        // Pedido completado - Compró RTX 4070 de María
        Order order1 = new Order();
        order1.setUserId(4L);
        order1.setSellerId(2L);
        order1.setProductIds("1"); // RTX 4070 Super
        order1.setTotalAmount(2599.00);
        order1.setStatus("COMPLETADO");
        order1.setCreatedAt(LocalDateTime.now().minusDays(15));
        orderRepository.save(order1);

        // Pedido en tránsito - Compró RAM y SSD de Pedro
        Order order2 = new Order();
        order2.setUserId(4L);
        order2.setSellerId(3L);
        order2.setProductIds("7,9"); // RAM Corsair + Samsung 990 PRO
        order2.setTotalAmount(1398.00);
        order2.setStatus("EN_CAMINO");
        order2.setCreatedAt(LocalDateTime.now().minusDays(3));
        orderRepository.save(order2);

        // ===== PEDIDOS DE LA CLIENTE ANA (ID: 5) =====
        
        // Pedido pendiente - Quiere comprar CPU Ryzen 7
        Order order3 = new Order();
        order3.setUserId(5L);
        order3.setSellerId(2L);
        order3.setProductIds("4"); // Ryzen 7 7800X3D
        order3.setTotalAmount(1899.00);
        order3.setStatus("PENDIENTE");
        order3.setCreatedAt(LocalDateTime.now().minusHours(2));
        orderRepository.save(order3);

        // Pedido completado - Compró mouse Razer de Pedro
        Order order4 = new Order();
        order4.setUserId(5L);
        order4.setSellerId(3L);
        order4.setProductIds("14"); // Razer DeathAdder
        order4.setTotalAmount(649.00);
        order4.setStatus("COMPLETADO");
        order4.setCreatedAt(LocalDateTime.now().minusDays(30));
        orderRepository.save(order4);

        // ===== PEDIDOS DEL CLIENTE LUIS (ID: 6) =====
        
        // Pedido grande - Build completa de María
        Order order5 = new Order();
        order5.setUserId(6L);
        order5.setSellerId(2L);
        order5.setProductIds("1,4,8,10,11,13"); // RTX4070+Ryzen7+RAM+SSD+Mobo+Teclado
        order5.setTotalAmount(7044.00);
        order5.setStatus("CONFIRMADO");
        order5.setCreatedAt(LocalDateTime.now().minusDays(1));
        orderRepository.save(order5);

        // Pedido cancelado
        Order order6 = new Order();
        order6.setUserId(6L);
        order6.setSellerId(3L);
        order6.setProductIds("2"); // RX 7800 XT
        order6.setTotalAmount(2199.00);
        order6.setStatus("CANCELADO");
        order6.setCreatedAt(LocalDateTime.now().minusDays(7));
        orderRepository.save(order6);

        // ===== PEDIDO ADICIONAL PARA DEMOSTRAR FLUJO =====
        
        // Pedido recién creado (para que el vendedor lo vea)
        Order order7 = new Order();
        order7.setUserId(4L);
        order7.setSellerId(2L);
        order7.setProductIds("6,13"); // Ryzen 5 7600X + Teclado Logitech
        order7.setTotalAmount(1448.00);
        order7.setStatus("PENDIENTE");
        order7.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        orderRepository.save(order7);

        System.out.println("7 pedidos creados en diferentes estados:");
        System.out.println("   PENDIENTE: 2 pedidos");
        System.out.println("   CONFIRMADO: 1 pedido");
        System.out.println("   EN_CAMINO: 1 pedido");
        System.out.println("   COMPLETADO: 2 pedidos");
        System.out.println("   CANCELADO: 1 pedido");
    }
}




