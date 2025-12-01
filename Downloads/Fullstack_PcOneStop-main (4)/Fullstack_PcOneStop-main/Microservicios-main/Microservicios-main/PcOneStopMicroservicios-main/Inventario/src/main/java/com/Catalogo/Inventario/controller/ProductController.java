package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Catálogo de Componentes PC", description = "Gestión del inventario de componentes de computadora: GPUs, CPUs, RAM, almacenamiento, periféricos y más. Los clientes pueden ver productos, los administradores pueden agregar y gestionar el inventario.")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(
        summary = "Ver detalles de un componente específico",
        description = "Obtiene la información detallada de un componente específico del catálogo de PcOneStop. " +
                     "Incluye nombre, marca, modelo, categoría, precio, stock disponible y descripción. " +
                     "Este endpoint es público, no requiere autenticación. Útil para mostrar la página de detalles del producto."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Producto obtenido\", \"data\": {\"id\": 1, \"name\": \"GeForce RTX 4070\", \"brand\": \"MSI\", \"model\": \"Ventus 3X\", \"category\": \"GPU\", \"price\": 699.99, \"stock\": 10}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 404, \"message\": \"Producto no encontrado con ID: 1\", \"data\": null, \"count\": 0}")
            )
        )
    })
    @Parameter(
        name = "id",
        description = "ID del componente cuyos detalles se desean consultar",
        required = true,
        example = "1"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getById(@PathVariable Long id) {
        try {
            Product product = productService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto obtenido", product, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, "Producto no encontrado con ID: " + id, null, 0L));
        }
    }

    @Operation(
        summary = "Ver catálogo completo de componentes PC (Solo ADMIN)",
        description = "Obtiene el listado completo de componentes de computadora disponibles en PcOneStop. " +
                     "Incluye GPUs, CPUs, tarjetas madre, RAM, almacenamiento (SSD/HDD), fuentes de poder, gabinetes, periféricos y más. " +
                     "Este endpoint requiere autenticación JWT con rol ADMIN. Solo los administradores pueden ver el listado completo de productos. " +
                     "Los clientes pueden ver productos individuales por ID o productos en oferta."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Catálogo obtenido exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Productos obtenidos\", \"data\": [{\"id\": 1, \"name\": \"GeForce RTX 4070\", \"brand\": \"MSI\", \"model\": \"Ventus 3X\", \"category\": \"GPU\", \"price\": 699.99, \"stock\": 10}], \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "No hay productos registrados en el catálogo",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 204, \"message\": \"No hay productos registrados\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Acceso denegado: se requiere rol ADMIN para ver todos los productos",
            content = @Content(mediaType = "application/json")
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> list() {
        List<Product> products = productService.findAll();
        
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(false, 204, "No hay productos registrados", null, 0L));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos obtenidos", products, (long) products.size()));
    }

    @Operation(
        summary = "Ver productos en oferta",
        description = "Obtiene el listado de componentes de PC que están actualmente en oferta en PcOneStop. " +
                     "Solo muestra productos con descuentos activos. Este endpoint es público, no requiere autenticación. " +
                     "Ideal para mostrar en la página de ofertas del sitio web."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Productos en oferta obtenidos exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Productos en oferta obtenidos\", \"data\": [{\"id\": 1, \"name\": \"GeForce RTX 4070\", \"brand\": \"MSI\", \"price\": 699.99, \"isOnSale\": true, \"discount\": 15}], \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "No hay productos en oferta actualmente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 204, \"message\": \"No hay productos en oferta\", \"data\": null, \"count\": 0}")
            )
        )
    })
    @GetMapping("/offers")
    public ResponseEntity<ApiResponse<List<Product>>> getOffers() {
        List<Product> offers = productService.findOnSaleProducts();
        
        if (offers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(false, 204, "No hay productos en oferta", null, 0L));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos en oferta obtenidos", offers, (long) offers.size()));
    }

    @Operation(
        summary = "Agregar nuevo componente al catálogo",
        description = "Permite a los administradores agregar un nuevo componente de PC al inventario de PcOneStop. " +
                     "Se debe especificar nombre, marca, modelo, categoría (GPU, CPU, RAM, etc.), precio y stock disponible. " +
                     "Requiere autenticación JWT con rol ADMIN."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Componente agregado exitosamente al catálogo",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 201, \"message\": \"Producto guardado\", \"data\": {\"id\": 1, \"name\": \"GeForce RTX 4070\", \"brand\": \"MSI\", \"model\": \"Ventus 3X\", \"category\": \"GPU\", \"price\": 699.99, \"stock\": 10}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error de validación: campos obligatorios faltantes, precio negativo, stock negativo",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El nombre del producto es obligatorio\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Acceso denegado: se requiere rol ADMIN para crear productos",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos del componente PC. Campos obligatorios: name, brand, model, category, price, stock. " +
                     "Campos opcionales: description, image, isOnSale, discount, offerStartDate, offerEndDate. " +
                     "También se puede enviar un objeto 'offer' con {discount, startDate, endDate} para configurar ofertas.",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Product.class),
            examples = {
                @ExampleObject(
                    name = "default",
                    summary = "Producto básico (por defecto)",
                    value = "{\"name\":\"GeForce RTX 4070\",\"brand\":\"MSI\",\"model\":\"Ventus 3X OC\",\"category\":\"GPU\",\"price\":699.99,\"stock\":10,\"description\":\"Tarjeta gráfica de alto rendimiento para gaming en 1440p y 4K\",\"image\":\"https://example.com/images/rtx4070.jpg\"}"
                ),
                @ExampleObject(
                    name = "Producto con oferta",
                    summary = "Producto en oferta con campos individuales",
                    value = "{\"name\":\"AMD Ryzen 7 7800X3D\",\"brand\":\"AMD\",\"model\":\"Ryzen 7 7800X3D\",\"category\":\"CPU\",\"price\":499.99,\"stock\":5,\"description\":\"Procesador de alto rendimiento con tecnología 3D V-Cache\",\"isOnSale\":true,\"discount\":15,\"offerStartDate\":\"2024-01-01\",\"offerEndDate\":\"2024-12-31\"}"
                ),
                @ExampleObject(
                    name = "Producto con objeto offer",
                    summary = "Usando el objeto offer del frontend",
                    value = "{\"name\":\"Corsair Vengeance DDR5 32GB\",\"brand\":\"Corsair\",\"model\":\"CMK32GX5M2B5600C36\",\"category\":\"RAM\",\"price\":199.99,\"stock\":20,\"offer\":{\"discount\":20,\"startDate\":\"2024-06-01\",\"endDate\":\"2024-06-30\"}}"
                )
            }
        )
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> save(HttpServletRequest request) {
        String jsonBody = null;
        try {
            logger.info("=== INICIO CREAR PRODUCTO ===");
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Content-Length: {}", request.getContentLength());
            
            // LEER EL BODY DIRECTAMENTE DESDE EL REQUEST (MISMA SOLUCIÓN QUE USUARIOS)
            try (BufferedReader reader = request.getReader()) {
                jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                logger.info("=== JSON BODY RAW (CREAR PRODUCTO) ===");
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
            Product product;
            try {
                product = objectMapper.readValue(jsonBody, Product.class);
                logger.info("=== DESERIALIZACIÓN PRODUCTO ===");
                logger.info("Product deserializado: {}", product);
                logger.info("name: '{}'", product.getName());
                logger.info("brand: '{}'", product.getBrand());
                logger.info("model: '{}'", product.getModel());
                logger.info("category: '{}'", product.getCategory());
                logger.info("price: {}", product.getPrice());
                logger.info("stock: {}", product.getStock());
                logger.info("image: '{}' (length: {})", 
                        product.getImage() != null ? 
                            (product.getImage().length() > 100 ? product.getImage().substring(0, 100) + "..." : product.getImage()) : "null",
                        product.getImage() != null ? product.getImage().length() : 0);
            } catch (Exception e) {
                logger.error("ERROR al deserializar JSON: {}", e.getMessage(), e);
                logger.error("JSON que falló: {}", jsonBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al procesar el JSON: " + e.getMessage(), null, 0L));
            }
            
            // Validaciones manuales básicas
            if (product.getName() == null || product.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El nombre del producto es obligatorio", null, 0L));
            }
            if (product.getBrand() == null || product.getBrand().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "La marca es obligatoria", null, 0L));
            }
            if (product.getModel() == null || product.getModel().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El modelo es obligatorio", null, 0L));
            }
            if (product.getCategory() == null || product.getCategory().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "La categoría es obligatoria", null, 0L));
            }
            if (product.getPrice() == null || product.getPrice() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El precio es obligatorio y debe ser mayor o igual a 0", null, 0L));
            }
            if (product.getStock() == null || product.getStock() < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El stock es obligatorio y debe ser mayor o igual a 0", null, 0L));
            }
            
            // Validar longitud de imagen si está presente
            if (product.getImage() != null && product.getImage().length() > 65535) {
                logger.warn("Imagen demasiado larga ({} caracteres), truncando a 65535", product.getImage().length());
                product.setImage(product.getImage().substring(0, 65535));
            }
            
            logger.info("Guardando producto en la base de datos...");
            Product newProduct = productService.save(product);
            logger.info("Producto guardado con ID: {}", newProduct.getId());
            logger.info("=== PRODUCTO CREADO EXITOSAMENTE - ID: {} ===", newProduct.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Producto guardado", newProduct, 1L));
        } catch (Exception e) {
            logger.error("=== ERROR AL CREAR PRODUCTO ===", e);
            logger.error("Tipo de excepción: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error: " + e.getMessage(), null, 0L));
        }
    }

    @Operation(
        summary = "Reducir stock de un componente",
        description = "Descuenta unidades del inventario cuando se realiza una compra en PcOneStop. " +
                     "El sistema valida que haya stock suficiente antes de descontar. Si el stock es insuficiente, " +
                     "se devuelve un error. Requiere autenticación JWT. Se usa automáticamente al procesar pedidos."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock actualizado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Stock actualizado\", \"data\": {\"id\": 1, \"name\": \"GeForce RTX 4070\", \"stock\": 8}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Stock insuficiente: no hay suficientes unidades disponibles",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"Stock insuficiente para el producto: GeForce RTX 4070\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "id",
        description = "ID del componente cuyo stock se desea reducir",
        required = true,
        example = "1"
    )
    @Parameter(
        name = "quantity",
        description = "Cantidad de unidades a descontar del stock",
        required = true,
        example = "2"
    )
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Product>> reduceStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            Product updatedProduct = productService.reduceStock(id, quantity);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Stock actualizado", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    @Operation(
        summary = "Eliminar componente del catálogo",
        description = "Elimina permanentemente un componente del catálogo de PcOneStop. También elimina automáticamente " +
                     "todos los reportes asociados al producto. Esta acción no se puede deshacer. " +
                     "Requiere autenticación JWT con rol ADMIN."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Componente eliminado exitosamente del catálogo",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Producto eliminado\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error al eliminar el producto",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "id",
        description = "ID del componente a eliminar del catálogo",
        required = true,
        example = "1"
    )
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto eliminado", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al eliminar: " + e.getMessage(), null, 0L));
        }
    }
}