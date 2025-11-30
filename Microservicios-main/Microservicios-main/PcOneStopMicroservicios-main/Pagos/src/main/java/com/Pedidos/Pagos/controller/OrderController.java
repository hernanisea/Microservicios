package com.Pedidos.Pagos.controller;

import com.Pedidos.Pagos.dto.ApiResponse;
import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos", description = "Gestión de órdenes de compra")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Crear un nuevo pedido", description = "Registra una nueva compra en estado PENDIENTE")
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(@RequestBody Order order) {
        try {
            Order newOrder = orderService.createOrder(order);
            ApiResponse<Order> response = new ApiResponse<>(
                    true, HttpStatus.CREATED.value(), "Pedido creado exitosamente", newOrder, 1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<Order> response = new ApiResponse<>(
                    false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al crear pedido: " + e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Obtener historial de un usuario", description = "Devuelve todos los pedidos realizados por un ID de usuario")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getByUser(@PathVariable Long userId) {
        List<Order> orders = orderService.findByUserId(userId);
        
        if (orders.isEmpty()) {
            ApiResponse<List<Order>> response = new ApiResponse<>(
                    false, HttpStatus.NOT_FOUND.value(), "No se encontraron pedidos para este usuario", null, 0L);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ApiResponse<List<Order>> response = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Pedidos encontrados", orders, (long) orders.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Obtener pedidos por vendedor")
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<Order>>> getBySeller(@PathVariable Long sellerId) {
        List<Order> orders = orderService.findBySellerId(sellerId);
        ApiResponse<List<Order>> response = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Pedidos del vendedor obtenidos", orders, (long) orders.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Actualizar estado del pedido")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable Long id, 
            @RequestParam String status
    ) {
        try {
            Order updatedOrder = orderService.updateStatus(id, status);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Estado actualizado", updatedOrder, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }
}