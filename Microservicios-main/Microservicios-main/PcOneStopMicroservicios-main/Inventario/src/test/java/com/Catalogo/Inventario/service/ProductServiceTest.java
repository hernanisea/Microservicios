package com.Catalogo.Inventario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.ProductRepository;
import com.Catalogo.Inventario.repository.ReportRepository;
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

    @Mock
    private ReportRepository reportRepository;

    @Test
    public void testReduceStockSuccess() {
        Product product = new Product(1L, "GPU", "Asus", "X", "GPU", 100.0, 10, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // Descontar 3
        Product result = productService.reduceStock(1L, 3);

        assertEquals(7, result.getStock());
    }

    @Test
    public void testReduceStockInsufficient() {
        Product product = new Product(1L, "GPU", "Asus", "X", "GPU", 100.0, 2, 1L); // Stock 2
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        // Intentar descontar 5 (debe fallar)
        assertThrows(RuntimeException.class, () -> {
            productService.reduceStock(1L, 5);
        });
    }

    @Test
    public void testDeleteProductClearsReportsFirst() {
        // Ejecutar borrado
        productService.deleteProduct(50L);

        // Verificar ORDEN: Primero reportes, luego producto
        verify(reportRepository).deleteByProductId(50L);
        verify(productRepository).deleteById(50L);
    }
}