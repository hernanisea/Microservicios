package com.Catalogo.Inventario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Catalogo.Inventario.model.Offer;
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

    // Helper para crear productos de prueba
    private Product crearProducto(String id, String name, String category, String brand, 
                                   Double price, Integer stock, Boolean isOnSale) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setCategory(category);
        p.setBrand(brand);
        p.setPrice(price);
        p.setStock(stock);
        p.setImage("https://example.com/img.png");
        p.setDescription("Descripción de prueba");
        p.setIsOnSale(isOnSale);
        return p;
    }

    // ==================== TESTS PARA findAll() ====================

    @Test
    public void testFindAll_RetornaProductos() {
        // DADO: 2 productos en BD
        Product p1 = crearProducto("gpu-rtx-4070", "RTX 4070", "GPU", "Nvidia", 700.0, 10, false);
        Product p2 = crearProducto("cpu-ryzen-7", "Ryzen 7", "CPU", "AMD", 400.0, 5, true);
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
        // DADO: un producto nuevo sin ID
        Product nuevo = new Product();
        nuevo.setName("RAM Corsair");
        nuevo.setCategory("RAM");
        nuevo.setBrand("Corsair");
        nuevo.setPrice(150.0);
        nuevo.setStock(20);

        when(productRepository.existsById(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: guardamos
        Product resultado = productService.save(nuevo);

        // ENTONCES: se genera ID automáticamente
        assertNotNull(resultado.getId());
        assertEquals("RAM Corsair", resultado.getName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    public void testSave_AsignaIsOnSaleFalsePorDefecto() {
        // DADO: un producto sin isOnSale definido
        Product nuevo = new Product();
        nuevo.setName("SSD Samsung");
        nuevo.setCategory("Almacenamiento");
        nuevo.setBrand("Samsung");
        nuevo.setPrice(100.0);
        nuevo.setStock(30);

        when(productRepository.existsById(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: guardamos
        Product resultado = productService.save(nuevo);

        // ENTONCES: isOnSale es false por defecto
        assertFalse(resultado.getIsOnSale());
    }

    // ==================== TESTS PARA findById() ====================

    @Test
    public void testFindById_ProductoExiste() {
        // DADO: un producto con ID específico
        Product producto = crearProducto("ssd-samsung-980", "Samsung 980 Pro", "Almacenamiento", "Samsung", 200.0, 15, false);
        when(productRepository.findById("ssd-samsung-980")).thenReturn(Optional.of(producto));

        // CUANDO: buscamos por ID
        Product resultado = productService.findById("ssd-samsung-980");

        // ENTONCES: retorna el producto
        assertEquals("ssd-samsung-980", resultado.getId());
        assertEquals("Samsung 980 Pro", resultado.getName());
    }

    @Test
    public void testFindById_ProductoNoExiste_LanzaExcepcion() {
        // DADO: un ID que no existe
        when(productRepository.findById("producto-inexistente")).thenReturn(Optional.empty());

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.findById("producto-inexistente");
        });
        assertTrue(ex.getMessage().contains("Producto no encontrado"));
    }

    // ==================== TESTS PARA findByCategory() ====================

    @Test
    public void testFindByCategory_RetornaProductosDeLaCategoria() {
        // DADO: 2 productos de categoría GPU
        Product p1 = crearProducto("gpu-rtx-4070", "RTX 4070", "GPU", "Nvidia", 700.0, 10, false);
        Product p2 = crearProducto("gpu-rtx-4060", "RTX 4060", "GPU", "Nvidia", 400.0, 15, false);
        when(productRepository.findByCategory("GPU")).thenReturn(Arrays.asList(p1, p2));

        // CUANDO: buscamos por categoría
        List<Product> resultado = productService.findByCategory("GPU");

        // ENTONCES: retorna 2 productos
        assertEquals(2, resultado.size());
    }

    // ==================== TESTS PARA findOnSale() ====================

    @Test
    public void testFindOnSale_RetornaProductosEnOferta() {
        // DADO: 2 productos en oferta
        Product p1 = crearProducto("cpu-ryzen-5600", "Ryzen 5 5600", "CPU", "AMD", 130.0, 20, true);
        p1.setOffer(new Offer(15, "2025-01-01", "2025-12-31"));
        Product p2 = crearProducto("psu-corsair-750", "Corsair RM750", "Fuente", "Corsair", 110.0, 25, true);
        p2.setOffer(new Offer(10, "2025-01-01", "2025-12-31"));
        
        when(productRepository.findByIsOnSaleTrue()).thenReturn(Arrays.asList(p1, p2));

        // CUANDO: buscamos productos en oferta
        List<Product> resultado = productService.findOnSale();

        // ENTONCES: retorna 2 productos
        assertEquals(2, resultado.size());
        assertTrue(resultado.get(0).getIsOnSale());
    }

    // ==================== TESTS PARA reduceStock() ====================

    @Test
    public void testReduceStock_ExitoConStockSuficiente() {
        // DADO: un producto con stock 10
        Product producto = crearProducto("gpu-asus", "GPU Asus", "GPU", "Asus", 100.0, 10, false);
        when(productRepository.findById("gpu-asus")).thenReturn(Optional.of(producto));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: descontamos 3 unidades
        Product resultado = productService.reduceStock("gpu-asus", 3);

        // ENTONCES: stock queda en 7
        assertEquals(7, resultado.getStock());
        verify(productRepository).save(producto);
    }

    @Test
    public void testReduceStock_DescontarTodoElStock() {
        // DADO: un producto con stock 5
        Product producto = crearProducto("gpu-asus", "GPU Asus", "GPU", "Asus", 100.0, 5, false);
        when(productRepository.findById("gpu-asus")).thenReturn(Optional.of(producto));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: descontamos exactamente 5
        Product resultado = productService.reduceStock("gpu-asus", 5);

        // ENTONCES: stock queda en 0
        assertEquals(0, resultado.getStock());
    }

    @Test
    public void testReduceStock_StockInsuficiente_LanzaExcepcion() {
        // DADO: un producto con stock 2
        Product producto = crearProducto("gpu-asus", "GPU Asus", "GPU", "Asus", 100.0, 2, false);
        when(productRepository.findById("gpu-asus")).thenReturn(Optional.of(producto));

        // CUANDO/ENTONCES: intentar descontar 5 lanza excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.reduceStock("gpu-asus", 5);
        });
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    // ==================== TESTS PARA deleteProduct() ====================

    @Test
    public void testDeleteProduct_BorraReportesPrimeroLuegoProducto() {
        // CUANDO: eliminamos producto
        productService.deleteProduct("gpu-test-123");

        // ENTONCES: primero borra reportes, luego producto (orden importante)
        var inOrder = inOrder(reportRepository, productRepository);
        inOrder.verify(reportRepository).deleteByProductId("gpu-test-123");
        inOrder.verify(productRepository).deleteById("gpu-test-123");
    }

    // ==================== TESTS PARA update() ====================

    @Test
    public void testUpdate_ActualizaProductoExistente() {
        // DADO: un producto existente
        Product existente = crearProducto("cpu-amd", "Ryzen 5", "CPU", "AMD", 200.0, 10, false);
        when(productRepository.findById("cpu-amd")).thenReturn(Optional.of(existente));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // Nuevos datos
        Product nuevosData = crearProducto("cpu-amd", "Ryzen 7 5800X", "CPU", "AMD", 350.0, 8, true);
        nuevosData.setOffer(new Offer(20, "2025-01-01", "2025-06-30"));

        // CUANDO: actualizamos
        Product resultado = productService.update("cpu-amd", nuevosData);

        // ENTONCES: los datos se actualizan
        assertEquals("Ryzen 7 5800X", resultado.getName());
        assertEquals(350.0, resultado.getPrice());
        assertEquals(8, resultado.getStock());
        assertTrue(resultado.getIsOnSale());
    }
}
