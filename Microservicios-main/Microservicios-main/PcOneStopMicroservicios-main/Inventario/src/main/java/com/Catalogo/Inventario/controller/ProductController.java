package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.dto.ApiResponse;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Inventario", description = "Gestión de catálogo y stock")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Listar todos los productos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> list() {
        List<Product> products = productService.findAll();
        
        if (products.isEmpty()) {
            ApiResponse<List<Product>> response = new ApiResponse<>(
                    false, HttpStatus.NO_CONTENT.value(), "No hay productos registrados", null, 0L);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        }

        ApiResponse<List<Product>> response = new ApiResponse<>(
                true, HttpStatus.OK.value(), "Productos obtenidos correctamente", products, (long) products.size());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Guardar producto", description = "Crea o actualiza un producto")
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> save(@RequestBody Product product) {
        try {
            Product newProduct = productService.save(product);
            ApiResponse<Product> response = new ApiResponse<>(
                    true, HttpStatus.CREATED.value(), "Producto guardado exitosamente", newProduct, 1L);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ApiResponse<Product> response = new ApiResponse<>(
                    false, HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error: " + e.getMessage(), null, 0L);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Descontar Stock", description = "Reduce la cantidad disponible de un producto")
    @PutMapping("/{id}/stock")
    public ResponseEntity<ApiResponse<Product>> reduceStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            Product updatedProduct = productService.reduceStock(id, quantity);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Stock actualizado", updatedProduct, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    false, HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Eliminar producto")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            productService.deleteProduct(id); // Llama al método que limpia reportes primero
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Producto eliminado", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error al eliminar: " + e.getMessage(), null, 0L));
        }
    }
}