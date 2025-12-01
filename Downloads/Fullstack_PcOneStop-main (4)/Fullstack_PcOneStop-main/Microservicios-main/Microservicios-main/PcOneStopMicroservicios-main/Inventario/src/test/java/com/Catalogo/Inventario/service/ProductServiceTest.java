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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReportRepository reportRepository;

    // ==================== TESTS PARA findAll() ====================

    @Test
    public void testFindAll_RetornaProductos() {
        // DADO: 2 productos en BD
        Product p1 = new Product(1L, "GPU", "Nvidia", "RTX 4070", "GPU", 700.0, 10, null, null, false, 0, null, null);
        Product p2 = new Product(2L, "CPU", "AMD", "Ryzen 7", "CPU", 400.0, 5, null, null, false, 0, null, null);
        when(productRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        // CUANDO: obtenemos todos
        List<Product> resultado = productService.findAll();

        // ENTONCES: retorna 2 productos
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindAll_ListaVacia() {
        // DADO: no hay productos
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // CUANDO: obtenemos todos
        List<Product> resultado = productService.findAll();

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS PARA save() ====================

    @Test
    public void testSave_GuardaProducto() {
        // DADO: un producto nuevo
        Product nuevo = new Product(null, "RAM", "Corsair", "Vengeance", "RAM", 150.0, 20, null, null, false, 0, null, null);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = (Product) i.getArguments()[0];
            p.setId(1L); // Simular ID generado
            return p;
        });

        // CUANDO: guardamos
        Product resultado = productService.save(nuevo);

        // ENTONCES: se guarda correctamente
        assertNotNull(resultado.getId());
        assertEquals("RAM", resultado.getName());
        verify(productRepository).save(nuevo);
    }

    // ==================== TESTS PARA findById() ====================

    @Test
    public void testFindById_ProductoExiste() {
        // DADO: un producto con ID 1
        Product producto = new Product(1L, "SSD", "Samsung", "980 Pro", "Storage", 200.0, 15, null, null, false, 0, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));

        // CUANDO: buscamos por ID
        Product resultado = productService.findById(1L);

        // ENTONCES: retorna el producto
        assertEquals(1L, resultado.getId());
        assertEquals("SSD", resultado.getName());
    }

    @Test
    public void testFindById_ProductoNoExiste_LanzaExcepcion() {
        // DADO: un ID que no existe
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.findById(999L);
        });
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    // ==================== TESTS PARA reduceStock() ====================

    @Test
    public void testReduceStock_ExitoConStockSuficiente() {
        // DADO: un producto con stock 10
        Product producto = new Product(1L, "GPU", "Asus", "X", "GPU", 100.0, 10, null, null, false, 0, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: descontamos 3 unidades
        Product resultado = productService.reduceStock(1L, 3);

        // ENTONCES: stock queda en 7
        assertEquals(7, resultado.getStock());
        verify(productRepository).save(producto);
    }

    @Test
    public void testReduceStock_DescontarTodoElStock() {
        // DADO: un producto con stock 5
        Product producto = new Product(1L, "GPU", "Asus", "X", "GPU", 100.0, 5, null, null, false, 0, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: descontamos exactamente 5
        Product resultado = productService.reduceStock(1L, 5);

        // ENTONCES: stock queda en 0
        assertEquals(0, resultado.getStock());
    }

    @Test
    public void testReduceStock_StockInsuficiente_LanzaExcepcion() {
        // DADO: un producto con stock 2
        Product producto = new Product(1L, "GPU", "Asus", "X", "GPU", 100.0, 2, null, null, false, 0, null, null);
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));

        // CUANDO/ENTONCES: intentar descontar 5 lanza excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.reduceStock(1L, 5);
        });
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    // ==================== TESTS PARA deleteProduct() ====================

    @Test
    public void testDeleteProduct_BorraReportesPrimeroLuegoProducto() {
        // CUANDO: eliminamos producto
        productService.deleteProduct(50L);

        // ENTONCES: primero borra reportes, luego producto (orden importante)
        var inOrder = inOrder(reportRepository, productRepository);
        inOrder.verify(reportRepository).deleteByProductId(50L);
        inOrder.verify(productRepository).deleteById(50L);
    }
}
