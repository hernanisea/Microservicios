package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Inventario", description = "Gestión de catálogo y stock")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Listar todos los productos", description = "Obtiene el catálogo completo de productos disponibles en PcOneStop")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Productos obtenidos exitosamente. Se devuelve la lista completa del catálogo de productos disponibles.")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> list() {
        List<Product> products = productService.findAll();
        
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse<>(true, 200, "No hay productos registrados", List.of(), 0L));
        }

        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos obtenidos", products, (long) products.size()));
    }

    @Operation(summary = "Obtener producto por ID", description = "Obtiene la información detallada de un producto específico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto encontrado. Se devuelve toda la información del producto incluyendo precio, stock, descripción y especificaciones."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado. El ID proporcionado no existe en el catálogo.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getById(@PathVariable String id) {
        try {
            Product product = productService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto encontrado", product, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Obtener productos por categoría", description = "Filtra productos según su categoría (CPU, GPU, RAM, etc.)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Productos filtrados exitosamente. Se devuelve la lista de productos de la categoría solicitada.")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Product>>> getByCategory(@PathVariable String category) {
        List<Product> products = productService.findByCategory(category);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos por categoría", products, (long) products.size()));
    }

    @Operation(summary = "Obtener productos en oferta", description = "Obtiene todos los productos que están actualmente en promoción")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Productos en oferta obtenidos exitosamente. Se devuelve la lista de productos con descuentos activos.")
    })
    @GetMapping("/offers")
    public ResponseEntity<ApiResponse<List<Product>>> getOnSale() {
        List<Product> products = productService.findOnSale();
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Productos en oferta", products, (long) products.size()));
    }

    @Operation(summary = "Crear nuevo producto", description = "Solo disponible para administradores",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Producto creado exitosamente. El nuevo producto ha sido agregado al catálogo de PcOneStop."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación. Verifica que todos los campos requeridos estén correctamente completados (nombre, precio, stock, categoría, marca)."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado. Debes iniciar sesión y proporcionar un token JWT válido."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado. Este endpoint solo está disponible para usuarios con rol de administrador."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor. Por favor, intenta nuevamente más tarde.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> create(@Valid @RequestBody Product product) {
        try {
            Product newProduct = productService.save(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Producto creado", newProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error: " + e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Actualizar producto existente", description = "Solo disponible para administradores",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente. Los cambios se han aplicado correctamente al catálogo."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación. Verifica que los datos proporcionados sean válidos."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado. Debes iniciar sesión y proporcionar un token JWT válido."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado. Este endpoint solo está disponible para usuarios con rol de administrador."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado. El ID proporcionado no existe en el catálogo.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> update(@PathVariable String id, @Valid @RequestBody Product product) {
        try {
            Product updatedProduct = productService.update(id, product);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto actualizado", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Descontar Stock", description = "Reduce el stock disponible de un producto (usado al procesar una compra)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Stock actualizado exitosamente. El inventario se ha descontado correctamente."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error al descontar stock. Verifica que la cantidad solicitada no exceda el stock disponible o que el producto exista.")
    })
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Product>> reduceStock(@PathVariable String id, @RequestParam Integer quantity) {
        try {
            Product updatedProduct = productService.reduceStock(id, quantity);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Stock actualizado", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Eliminar producto", description = "Solo disponible para administradores",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente. El producto ha sido removido permanentemente del catálogo."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado. Debes iniciar sesión y proporcionar un token JWT válido."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado. Este endpoint solo está disponible para usuarios con rol de administrador."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Producto no encontrado. El ID proporcionado no existe en el catálogo."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al eliminar el producto. Por favor, intenta nuevamente más tarde.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto eliminado", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al eliminar: " + e.getMessage(), null, 0L));
        }
    }
}
