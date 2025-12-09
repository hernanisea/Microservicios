package com.Pedidos.Pagos.controller;

import com.Pedidos.Pagos.dto.ApiResponse;
import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos y Órdenes", description = "Gestión de órdenes de compra, historial y estados de pedidos")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // Crear pedido
    @Operation(
        summary = "Crear nuevo pedido",
        description = "Registra una nueva orden de compra. El estado inicial siempre es 'PENDIENTE'. " +
                      "Los productIds se envían como string separado por comas."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Pedido creado exitosamente - el estado inicial siempre es 'PENDIENTE'"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al crear el pedido (problemas con base de datos o validación)"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del pedido a crear",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = Order.class),
                    examples = @ExampleObject(
                        name = "Ejemplo de pedido",
                        value = """
                            {
                                "userId": 4,
                                "sellerId": 2,
                                "productIds": "1,4,7",
                                "totalAmount": 5097.00
                            }
                            """
                    )
                )
            )
            @RequestBody Order order
    ) {
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

    // Historial por usuario
    @Operation(
        summary = "Obtener historial de compras del usuario",
        description = "Devuelve todos los pedidos realizados por un cliente específico. " +
                      "Útil para mostrar el historial de compras en la app del cliente."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de pedidos obtenida"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No se encontraron pedidos para este usuario"
        )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Order>>> getByUser(
            @Parameter(description = "ID del usuario/cliente", example = "4", required = true)
            @PathVariable Long userId
    ) {
        List<Order> orders = orderService.findByUserId(userId);
        
        if (orders.isEmpty()) {
            ApiResponse<List<Order>> response = new ApiResponse<>(
                    false, HttpStatus.NOT_FOUND.value(), "No se encontraron pedidos para este usuario", null, 0L);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        ApiResponse<List<Order>> response = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Historial de pedidos obtenido", orders, (long) orders.size());
        return ResponseEntity.ok(response);
    }

    // Pedidos por vendedor
    @Operation(
        summary = "Obtener pedidos del vendedor",
        description = "Devuelve todos los pedidos que debe gestionar un vendedor específico. " +
                      "Incluye pedidos en todos los estados (pendientes, en camino, completados, etc.)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de pedidos del vendedor obtenida"
        )
    })
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<Order>>> getBySeller(
            @Parameter(description = "ID del vendedor", example = "2", required = true)
            @PathVariable Long sellerId
    ) {
        List<Order> orders = orderService.findBySellerId(sellerId);
        ApiResponse<List<Order>> response = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Pedidos del vendedor obtenidos", orders, (long) orders.size());
        return ResponseEntity.ok(response);
    }

    // Actualizar estado
    @Operation(
        summary = "Actualizar estado del pedido",
        description = "Permite cambiar el estado de un pedido. Estados válidos: " +
                      "PENDIENTE, CONFIRMADO, EN_CAMINO, COMPLETADO, CANCELADO"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estado actualizado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado - el ID proporcionado no existe en el sistema"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al actualizar el estado del pedido"
        )
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @Parameter(description = "ID del pedido", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(
                description = "Nuevo estado del pedido",
                example = "EN_CAMINO",
                required = true,
                schema = @Schema(allowableValues = {"PENDIENTE", "CONFIRMADO", "EN_CAMINO", "COMPLETADO", "CANCELADO"})
            )
            @RequestParam String status
    ) {
        try {
            Order updatedOrder = orderService.updateStatus(id, status);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Estado actualizado a: " + status, updatedOrder, 1L));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al actualizar: " + e.getMessage(), null, 0L));
        }
    }

    // Obtener pedido por ID
    @Operation(
        summary = "Obtener pedido por ID",
        description = "Busca y devuelve los detalles de un pedido específico por su identificador."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getById(
            @Parameter(description = "ID del pedido", example = "1", required = true)
            @PathVariable Long id
    ) {
        try {
            Order order = orderService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Pedido encontrado", order, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    // Listar todos los pedidos
    @Operation(
        summary = "Listar todos los pedidos",
        description = "Obtiene todos los pedidos del sistema. Solo para uso administrativo."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de todos los pedidos obtenida exitosamente"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAll() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), "Lista de todos los pedidos", orders, (long) orders.size()));
    }

    // Eliminar pedido
    @Operation(
        summary = "Eliminar pedido",
        description = "Elimina un pedido por su ID. Solo se pueden eliminar pedidos en estado COMPLETADO."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Pedido eliminado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Solo se pueden eliminar pedidos en estado COMPLETADO"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno al eliminar el pedido"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
            @Parameter(description = "ID del pedido a eliminar", example = "1", required = true)
            @PathVariable Long id
    ) {
        try {
            Order order = orderService.findById(id);
            
            if (!order.getStatus().equals("COMPLETADO")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, HttpStatus.BAD_REQUEST.value(), 
                        "Solo se pueden eliminar pedidos en estado COMPLETADO", null, 0L));
            }
            
            orderService.deleteById(id);
            return ResponseEntity.ok(
                new ApiResponse<>(true, HttpStatus.OK.value(), "Pedido eliminado exitosamente", null, 0L)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, HttpStatus.NOT_FOUND.value(), "Pedido no encontrado", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                    "Error al eliminar pedido: " + e.getMessage(), null, 0L));
        }
    }
}
