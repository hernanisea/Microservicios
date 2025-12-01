package com.Pedidos.Pagos.controller;

import com.Pedidos.Pagos.dto.ApiResponse;
import com.Pedidos.Pagos.model.Order;
import com.Pedidos.Pagos.service.OrderService;
import com.Pedidos.Pagos.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Pedidos y Compras", description = "Gestión de órdenes de compra de componentes PC. Los clientes pueden realizar pedidos y los administradores pueden gestionar el estado de los pedidos.")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(
        summary = "Listar todos los pedidos",
        description = "Obtiene el listado completo de todos los pedidos realizados en PcOneStop, incluyendo información de cliente, monto total, " +
                     "estado del pedido (PENDIENTE, EN_PROCESO, ENVIADO, COMPLETADO, CANCELADO), productos comprados y fecha de creación. " +
                     "Requiere autenticación JWT con rol ADMIN. Útil para que los administradores gestionen todos los pedidos del sistema, " +
                     "realizar seguimiento de ventas y generar reportes."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de pedidos obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), "Lista de pedidos obtenida", orders, (long) orders.size()));
    }

    @Operation(
        summary = "Realizar una compra de componentes PC",
        description = "Crea un nuevo pedido en PcOneStop cuando un cliente compra componentes de computadora. " +
                     "El pedido se crea automáticamente en estado PENDIENTE. El userId se extrae automáticamente del token JWT si no se proporciona. " +
                     "Debe incluir el monto total (puede enviarse como 'total' o 'totalAmount') y los IDs de los productos comprados (separados por comas en el campo productIds). " +
                     "Requiere autenticación JWT. El sistema automáticamente descontará el stock de los productos cuando el pedido se procese. " +
                     "El token JWT debe incluirse en el header 'Authorization: Bearer <token>'."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Pedido creado exitosamente. El cliente recibirá confirmación y el pedido quedará en estado PENDIENTE.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 201, \"message\": \"Pedido creado exitosamente\", \"data\": {\"id\": 1, \"userId\": 10, \"totalAmount\": 15000.00, \"status\": \"PENDIENTE\", \"productIds\": \"1,5,8\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error de validación: monto total faltante o inválido (debe ser mayor a cero), userId no se pudo extraer del token, o datos incompletos",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Monto faltante", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El monto total (total o totalAmount) es obligatorio y debe ser mayor a cero.\", \"data\": null, \"count\": 0}"),
                    @ExampleObject(name = "Usuario faltante", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"No se pudo determinar el ID del usuario para crear el pedido.\", \"data\": null, \"count\": 0}")
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno al crear el pedido: stock insuficiente de algún producto, error de base de datos, o problema al procesar la orden",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 500, \"message\": \"Error al crear pedido: Stock insuficiente para el producto ID 5\", \"data\": null, \"count\": 0}")
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos del pedido. El userId se extrae automáticamente del token JWT si no se proporciona. " +
                     "El campo totalAmount puede enviarse como 'total' o 'totalAmount'. El status se asigna automáticamente como PENDIENTE.",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Order.class),
            examples = {
                @ExampleObject(
                    name = "default",
                    summary = "Pedido con totalAmount (por defecto)",
                    value = "{\"totalAmount\":15999.99,\"productIds\":\"1,5,8\"}"
                ),
                @ExampleObject(
                    name = "Pedido con total",
                    summary = "Usando el campo total (alias)",
                    value = "{\"total\":15999.99,\"productIds\":\"1,5,8\"}"
                ),
                @ExampleObject(
                    name = "Pedido con userId explícito",
                    summary = "Especificando userId manualmente",
                    value = "{\"userId\":10,\"totalAmount\":25000.00,\"productIds\":\"2,3,4,5\"}"
                )
            }
        )
    )
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> create(HttpServletRequest request) {
        String jsonBody = null;
        try {
            logger.info("=== INICIO CREAR PEDIDO ===");
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Content-Length: {}", request.getContentLength());
            
            // LEER EL BODY DIRECTAMENTE DESDE EL REQUEST (MISMA SOLUCIÓN QUE OTROS ENDPOINTS)
            try (BufferedReader reader = request.getReader()) {
                jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                logger.info("=== JSON BODY RAW (CREAR PEDIDO) ===");
                logger.info("Body recibido: {}", jsonBody);
            } catch (IOException e) {
                logger.error("ERROR al leer el body del request: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al leer el cuerpo de la petición: " + e.getMessage(), null, 0L));
            }
            
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("ERROR CRÍTICO: El JSON body está vacío o es NULL!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error: El cuerpo de la petición está vacío", null, 0L));
            }
            
            // Deserializar manualmente
            Order order;
            try {
                order = objectMapper.readValue(jsonBody, Order.class);
                logger.info("=== DESERIALIZACIÓN PEDIDO ===");
                logger.info("Order deserializado: {}", order);
                logger.info("userId: {}", order.getUserId());
                logger.info("totalAmount: {}", order.getTotalAmount());
                logger.info("productIds: '{}'", order.getProductIds());
            } catch (Exception e) {
                logger.error("ERROR al deserializar JSON: {}", e.getMessage(), e);
                logger.error("JSON que falló: {}", jsonBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al procesar el JSON: " + e.getMessage(), null, 0L));
            }
            
            // Asegurar que el id sea null (se genera automáticamente)
            order.setId(null);
            
            // PRIMERO: Extraer userId del token JWT si no se proporciona (antes de validar)
            if (order.getUserId() == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    Long userId = jwtUtil.extractUserId(token);
                    if (userId != null) {
                        order.setUserId(userId);
                        logger.info("userId extraído del token JWT: {}", userId);
                    } else {
                        logger.warn("No se pudo extraer el userId del token para asignar al pedido.");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(false, 400, "No se pudo determinar el ID del usuario para crear el pedido.", null, 0L));
                    }
                } else {
                    logger.warn("No se encontró token de autorización para asignar el userId.");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ApiResponse<>(false, 401, "Se requiere token de autorización para crear un pedido.", null, 0L));
                }
            }
            
            // SEGUNDO: Validar que userId esté presente después de extraerlo del token
            if (order.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El ID del usuario es obligatorio.", null, 0L));
            }
            
            // TERCERO: Validar que totalAmount esté presente
            if (order.getTotalAmount() == null || order.getTotalAmount() <= 0) {
                logger.warn("Intento de crear pedido sin totalAmount o con valor inválido: {}", order.getTotalAmount());
                logger.warn("JSON recibido: {}", jsonBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El monto total (total o totalAmount) es obligatorio y debe ser mayor a cero.", null, 0L));
            }
            
            logger.info("Guardando pedido en la base de datos...");
            Order newOrder = orderService.createOrder(order);
            logger.info("Pedido guardado con ID: {}", newOrder.getId());
            logger.info("=== PEDIDO CREADO EXITOSAMENTE - ID: {} ===", newOrder.getId());
            
            ApiResponse<Order> response = new ApiResponse<>(
                    true, HttpStatus.CREATED.value(), "Pedido creado exitosamente", newOrder, 1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("=== ERROR AL CREAR PEDIDO ===", e);
            logger.error("Tipo de excepción: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            ApiResponse<Order> response = new ApiResponse<>(
                    false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error al crear pedido: " + e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(
        summary = "Ver historial de compras de un cliente",
        description = "Obtiene todos los pedidos realizados por un cliente específico en PcOneStop. " +
                     "Muestra el historial completo de compras de componentes PC, incluyendo estado actual del pedido, monto total, " +
                     "IDs de productos comprados y fecha de creación. Requiere autenticación JWT. " +
                     "Útil para mostrar el historial de compras en el perfil del usuario o para que los clientes rastreen sus pedidos."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Historial de pedidos obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Pedidos encontrados\", \"data\": [{\"id\": 1, \"userId\": 10, \"totalAmount\": 15000.00, \"status\": \"COMPLETADO\"}], \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No se encontraron pedidos para este usuario",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 404, \"message\": \"No se encontraron pedidos para este usuario\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "userId",
        description = "ID del cliente cuyo historial de compras se desea consultar",
        required = true,
        example = "10"
    )
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


    @Operation(
        summary = "Actualizar estado de un pedido",
        description = "Permite a los administradores cambiar el estado de un pedido en PcOneStop para reflejar el progreso del envío. " +
                     "Los estados posibles son: PENDIENTE (recién creado, esperando procesamiento), EN_PROCESO (en preparación para envío), " +
                     "ENVIADO (enviado al cliente, en tránsito), COMPLETADO (entregado exitosamente al cliente) o CANCELADO (pedido cancelado). " +
                     "Requiere autenticación JWT con rol ADMIN. Útil para gestionar el flujo de pedidos y notificar a los clientes sobre el estado de sus compras."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Estado del pedido actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Estado actualizado\", \"data\": {\"id\": 1, \"status\": \"ENVIADO\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Estado inválido o pedido no encontrado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error al actualizar el estado",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "id",
        description = "ID del pedido cuyo estado se desea actualizar",
        required = true,
        example = "1"
    )
    @Parameter(
        name = "status",
        description = "Nuevo estado del pedido. Valores válidos: PENDIENTE (recién creado), EN_PROCESO (en preparación), ENVIADO (en tránsito), COMPLETADO (entregado), CANCELADO (cancelado)",
        required = true,
        example = "ENVIADO"
    )
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