package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.Category;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Inventario de Productos", description = "Gestión de catálogo, stock y productos del marketplace")
public class ProductController {

    @Autowired
    private ProductService productService;

    // Listar todos
    @Operation(
        summary = "Listar todos los productos",
        description = "Obtiene el catálogo completo de productos disponibles en el marketplace."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de productos obtenida exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "No hay productos registrados"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> list() {
        List<Product> products = productService.findAll();
        
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse<>(false, 204, "No hay productos registrados", null, 0L));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos obtenidos exitosamente", products, (long) products.size()));
    }

    // Obtener por ID
    @Operation(
        summary = "Obtener producto por ID",
        description = "Busca y devuelve un producto específico por su identificador único."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto encontrado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado - el ID proporcionado no existe en el catálogo"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getById(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id
    ) {
        try {
            Product product = productService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto encontrado", product, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    // Guardar producto
    @Operation(
        summary = "Crear nuevo producto",
        description = "Registra un nuevo producto en el catálogo. Requiere una categoría válida."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Producto creado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos (validación fallida) o categoría no encontrada en el sistema"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor al procesar la solicitud"
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> save(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del producto a crear",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = Product.class),
                    examples = @ExampleObject(
                        name = "Ejemplo de producto",
                        value = """
                            {
                                "name": "GeForce RTX 4070",
                                "brand": "MSI",
                                "model": "Ventus 3X OC",
                                "category": "GPU",
                                "price": 2599.99,
                                "stock": 15,
                                "sellerId": 2,
                                "description": "Tarjeta gráfica de última generación",
                                "imageUrl": "https://example.com/rtx4070.jpg"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody Product product
    ) {
        try {
            Product newProduct = productService.save(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Producto guardado exitosamente", newProduct, 1L));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error: " + e.getMessage(), null, 0L));
        }
    }

    // Actualizar producto
    @Operation(
        summary = "Actualizar producto",
        description = "Modifica la información de un producto existente."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto actualizado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado - el ID proporcionado no existe en el catálogo"
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> update(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody Product product
    ) {
        try {
            Product updatedProduct = productService.update(id, product);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto actualizado exitosamente", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    // Descontar stock
    @Operation(
        summary = "Descontar stock",
        description = "Reduce el inventario de un producto por la cantidad especificada. Valida que haya suficiente stock."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Stock actualizado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Stock insuficiente - la cantidad a descontar es mayor al stock disponible o producto no encontrado"
        )
    })
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Product>> reduceStock(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Cantidad a descontar", example = "2", required = true)
            @RequestParam Integer quantity
    ) {
        try {
            Product updatedProduct = productService.reduceStock(id, quantity);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Stock actualizado", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    // Aumentar stock
    @Operation(
        summary = "Agregar stock",
        description = "Incrementa el inventario de un producto. Valida que el producto exista."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock incrementado exitosamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Producto no encontrado o error en la operación")
    })
    @PutMapping("/{id}/stock/add")
    public ResponseEntity<ApiResponse<Product>> addStock(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Cantidad a agregar", example = "10", required = true)
            @RequestParam Integer quantity
    ) {
        try {
            Product updatedProduct = productService.addStock(id, quantity);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Stock incrementado", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    // Eliminar producto
    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto del catálogo. También elimina automáticamente todos los reportes asociados al producto."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto eliminado exitosamente junto con sus reportes asociados"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno al eliminar el producto o sus reportes asociados"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID del producto", example = "1", required = true)
            @PathVariable Long id
    ) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto eliminado exitosamente", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al eliminar: " + e.getMessage(), null, 0L));
        }
    }

    // Listar categorías
    @Operation(
        summary = "Listar categorías",
        description = "Obtiene todas las categorías de productos disponibles en el sistema (GPU, CPU, RAM, etc.)."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de categorías obtenida exitosamente"
        )
    })
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getAllCategories() {
        List<Category> categories = productService.findAllCategories();
        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Categorías disponibles", categories, (long) categories.size()));
    }

    // Productos por categoría
    @Operation(
        summary = "Filtrar por categoría",
        description = "Obtiene todos los productos de una categoría específica. La categoría debe existir en el sistema."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de productos de la categoría obtenida exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada en el sistema"
        )
    })
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<ApiResponse<List<Product>>> getByCategory(
            @Parameter(description = "Nombre de la categoría", example = "GPU", required = true)
            @PathVariable String categoryName
    ) {
        try {
            List<Product> products = productService.findByCategory(categoryName);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, 200, "Productos de categoría: " + categoryName, products, (long) products.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    // Productos por vendedor
    @Operation(
        summary = "Filtrar por vendedor",
        description = "Obtiene todos los productos publicados por un vendedor específico. " +
                      "Si el vendedor no tiene productos, devuelve una lista vacía."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de productos del vendedor obtenida (puede estar vacía si no tiene productos)"
        )
    })
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<Product>>> getBySeller(
            @Parameter(description = "ID del vendedor", example = "2", required = true)
            @PathVariable Long sellerId
    ) {
        List<Product> products = productService.findBySellerId(sellerId);
        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Productos del vendedor", products, (long) products.size()));
    }
}
