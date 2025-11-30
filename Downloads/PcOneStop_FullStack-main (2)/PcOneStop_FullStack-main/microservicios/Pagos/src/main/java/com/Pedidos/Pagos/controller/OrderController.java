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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Crear un nuevo pedido")
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(@RequestBody Order order) {
        try {
            Order newOrder = orderService.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Pedido creado exitosamente", newOrder, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al crear pedido: " + e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Obtener todos los pedidos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAll() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Pedidos obtenidos", orders, (long) orders.size()));
    }

    @Operation(summary = "Obtener pedido por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getById(@PathVariable String id) {
        try {
            Order order = orderService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Pedido encontrado", order, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Obtener pedidos por email del cliente")
    @GetMapping("/customer/{email}")
    public ResponseEntity<ApiResponse<List<Order>>> getByCustomerEmail(@PathVariable String email) {
        List<Order> orders = orderService.findByCustomerEmail(email);
        
        if (orders.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 200, "No se encontraron pedidos para este cliente", List.of(), 0L));
        }

        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Pedidos encontrados", orders, (long) orders.size()));
    }

    @Operation(summary = "Eliminar pedido")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Pedido eliminado", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al eliminar: " + e.getMessage(), null, 0L));
        }
    }
}
