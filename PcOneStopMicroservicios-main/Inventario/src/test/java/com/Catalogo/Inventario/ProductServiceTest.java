package com.Catalogo.Inventario;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.ProductRepository;
import com.Catalogo.Inventario.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    public void testReduceStockSuccess() {
        // Datos de prueba
        Long productId = 1L;
        Product mockProduct = new Product(productId, "Laptop", "Asus", "TUF", "PC", 1000.0, 10, 1L); // Stock inicial 10

        // Simular comportamiento del repositorio
        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ejecutar
        Product result = productService.reduceStock(productId, 3);

        // Verificar
        assertNotNull(result);
        assertEquals(7, result.getStock()); // 10 - 3 = 7
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testReduceStockInsufficient() {
        Long productId = 1L;
        Product mockProduct = new Product(productId, "Laptop", "Asus", "TUF", "PC", 1000.0, 2, 1L); // Stock solo 2

        when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

        // Verificar que lance excepción si pido 5
        Exception exception = assertThrows(RuntimeException.class, () -> {
            productService.reduceStock(productId, 5);
        });

        assertTrue(exception.getMessage().contains("Stock insuficiente"));
        verify(productRepository, never()).save(any(Product.class));
    }
}