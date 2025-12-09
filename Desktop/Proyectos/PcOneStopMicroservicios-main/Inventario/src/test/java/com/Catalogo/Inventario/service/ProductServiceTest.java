package com.Catalogo.Inventario.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Catalogo.Inventario.model.Category;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.CategoryRepository;
import com.Catalogo.Inventario.repository.ProductRepository;
import com.Catalogo.Inventario.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
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
    private CategoryRepository categoryRepository;

    @Mock
    private ReportRepository reportRepository;

    // Categorías de prueba
    private Category gpuCategory;
    private Category cpuCategory;

    @BeforeEach
    void setUp() {
        gpuCategory = new Category(1L, "GPU", "Tarjetas gráficas", null);
        cpuCategory = new Category(2L, "CPU", "Procesadores", null);
    }

    // Método auxiliar para crear productos 
    private Product crearProducto(Long id, String name, String brand, String model, 
                                   Category category, Double price, Integer stock, Long sellerId) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setBrand(brand);
        p.setModel(model);
        p.setCategoryEntity(category);
        p.setPrice(price);
        p.setStock(stock);
        p.setSellerId(sellerId);
        return p;
    }

    // Tests findAll() 
    @Test
    public void testFindAll_RetornaProductos() {
        // DADO: 2 productos en BD
        Product p1 = crearProducto(1L, "RTX 4070", "Nvidia", "RTX 4070", gpuCategory, 700.0, 10, 1L);
        Product p2 = crearProducto(2L, "Ryzen 7", "AMD", "7800X3D", cpuCategory, 400.0, 5, 1L);
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

    // Tests save() 
    @Test
    public void testSave_GuardaProductoConCategoria() {
        // DADO: un producto nuevo con categoría válida
        Product nuevo = new Product();
        nuevo.setName("RAM Corsair");
        nuevo.setBrand("Corsair");
        nuevo.setModel("Vengeance");
        nuevo.setCategory("GPU"); // Se envía el nombre de la categoría
        nuevo.setPrice(150.0);
        nuevo.setStock(20);
        nuevo.setSellerId(1L);

        when(categoryRepository.findByName("GPU")).thenReturn(Optional.of(gpuCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = (Product) i.getArguments()[0];
            p.setId(1L); // Simular ID generado
            return p;
        });

        // CUANDO: guardamos
        Product resultado = productService.save(nuevo);

        // ENTONCES: se guarda correctamente con la categoría asignada
        assertNotNull(resultado.getId());
        assertEquals("RAM Corsair", resultado.getName());
        assertEquals(gpuCategory, resultado.getCategoryEntity());
        verify(productRepository).save(nuevo);
    }

    @Test
    public void testSave_CategoriaNula_LanzaExcepcion() {
        // DADO: un producto sin categoría
        Product producto = new Product();
        producto.setName("Test");
        producto.setCategory(null);

        // CUANDO/ENTONCES: lanza excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.save(producto);
        });
        assertTrue(ex.getMessage().contains("categoría es obligatoria"));
    }

    @Test
    public void testSave_CategoriaNoExiste_LanzaExcepcion() {
        // DADO: un producto con categoría inexistente
        Product producto = new Product();
        producto.setName("Test");
        producto.setCategory("INEXISTENTE");

        when(categoryRepository.findByName("INEXISTENTE")).thenReturn(Optional.empty());

        // CUANDO/ENTONCES: lanza excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.save(producto);
        });
        assertTrue(ex.getMessage().contains("Categoría no encontrada"));
    }

    // Tests findById() 
    @Test
    public void testFindById_ProductoExiste() {
        // DADO: un producto con ID 1
        Product producto = crearProducto(1L, "SSD Samsung", "Samsung", "980 Pro", gpuCategory, 200.0, 15, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));

        // CUANDO: buscamos por ID
        Product resultado = productService.findById(1L);

        // ENTONCES: retorna el producto
        assertEquals(1L, resultado.getId());
        assertEquals("SSD Samsung", resultado.getName());
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

    // Tests reduceStock() 
    @Test
    public void testReduceStock_ExitoConStockSuficiente() {
        // DADO: un producto con stock 10
        Product producto = crearProducto(1L, "GPU Test", "Asus", "X", gpuCategory, 100.0, 10, 1L);
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
        Product producto = crearProducto(1L, "GPU Test", "Asus", "X", gpuCategory, 100.0, 5, 1L);
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
        Product producto = crearProducto(1L, "GPU Test", "Asus", "X", gpuCategory, 100.0, 2, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));

        // CUANDO/ENTONCES: intentar descontar 5 lanza excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.reduceStock(1L, 5);
        });
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    // Tests addStock() 
    @Test
    public void testAddStock_IncrementaCorrectamente() {
        // DADO: un producto con stock 10
        Product producto = crearProducto(1L, "GPU Test", "Asus", "X", gpuCategory, 100.0, 10, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: agregamos 5 unidades
        Product resultado = productService.addStock(1L, 5);

        // ENTONCES: stock queda en 15
        assertEquals(15, resultado.getStock());
        verify(productRepository).save(producto);
    }

    // Tests update() 
    @Test
    public void testUpdate_ActualizaProductoCorrectamente() {
        // DADO: un producto existente
        Product existente = crearProducto(1L, "Nombre Viejo", "Marca Vieja", "Modelo Viejo", gpuCategory, 100.0, 10, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // Y nuevos datos
        Product nuevoDatos = new Product();
        nuevoDatos.setName("Nombre Nuevo");
        nuevoDatos.setBrand("Marca Nueva");
        nuevoDatos.setModel("Modelo Nuevo");
        nuevoDatos.setPrice(200.0);
        nuevoDatos.setStock(20);
        nuevoDatos.setDescription("Nueva descripción");
        nuevoDatos.setImageUrl("https://nueva-url.com/img.jpg");

        // CUANDO: actualizamos
        Product resultado = productService.update(1L, nuevoDatos);

        // ENTONCES: los campos se actualizan
        assertEquals("Nombre Nuevo", resultado.getName());
        assertEquals("Marca Nueva", resultado.getBrand());
        assertEquals(200.0, resultado.getPrice());
        assertEquals(20, resultado.getStock());
        verify(productRepository).save(existente);
    }

    @Test
    public void testUpdate_CambiaCategoria() {
        // DADO: un producto existente con categoría GPU
        Product existente = crearProducto(1L, "Producto", "Marca", "Modelo", gpuCategory, 100.0, 10, 1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoryRepository.findByName("CPU")).thenReturn(Optional.of(cpuCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        // Y nuevos datos con categoría CPU
        Product nuevoDatos = new Product();
        nuevoDatos.setName("Producto");
        nuevoDatos.setBrand("Marca");
        nuevoDatos.setModel("Modelo");
        nuevoDatos.setCategory("CPU");
        nuevoDatos.setPrice(100.0);
        nuevoDatos.setStock(10);

        // CUANDO: actualizamos
        Product resultado = productService.update(1L, nuevoDatos);

        // ENTONCES: la categoría cambia a CPU
        assertEquals(cpuCategory, resultado.getCategoryEntity());
    }

    // Tests deleteProduct() 
    @Test
    public void testDeleteProduct_BorraReportesPrimeroLuegoProducto() {
        // CUANDO: eliminamos producto
        productService.deleteProduct(50L);

        // ENTONCES: primero borra reportes, luego producto (orden importante)
        var inOrder = inOrder(reportRepository, productRepository);
        inOrder.verify(reportRepository).deleteByProductId(50L);
        inOrder.verify(productRepository).deleteById(50L);
    }

    // Tests findAllCategories() 
    @Test
    public void testFindAllCategories_RetornaCategorias() {
        // DADO: 2 categorías
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(gpuCategory, cpuCategory));

        // CUANDO: obtenemos todas
        List<Category> resultado = productService.findAllCategories();

        // ENTONCES: retorna 2 categorías
        assertEquals(2, resultado.size());
    }

    // Tests findByCategory() 
    @Test
    public void testFindByCategory_RetornaProductosDeLaCategoria() {
        // DADO: productos en la categoría GPU
        Product p1 = crearProducto(1L, "RTX 4070", "Nvidia", "4070", gpuCategory, 700.0, 10, 1L);
        Product p2 = crearProducto(2L, "RX 7800", "AMD", "7800XT", gpuCategory, 600.0, 5, 1L);
        
        when(categoryRepository.findByName("GPU")).thenReturn(Optional.of(gpuCategory));
        when(productRepository.findByCategoryEntity(gpuCategory)).thenReturn(Arrays.asList(p1, p2));

        // CUANDO: buscamos por categoría
        List<Product> resultado = productService.findByCategory("GPU");

        // ENTONCES: retorna 2 productos
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindByCategory_CategoriaNoExiste_LanzaExcepcion() {
        // DADO: una categoría que no existe
        when(categoryRepository.findByName("INEXISTENTE")).thenReturn(Optional.empty());

        // CUANDO/ENTONCES: lanza excepción
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            productService.findByCategory("INEXISTENTE");
        });
        assertTrue(ex.getMessage().contains("Categoría no encontrada"));
    }

    // Tests findBySellerId() 
    @Test
    public void testFindBySellerId_RetornaProductosDelVendedor() {
        // DADO: productos del vendedor 2
        Product p1 = crearProducto(1L, "Producto 1", "Marca", "Modelo", gpuCategory, 100.0, 10, 2L);
        Product p2 = crearProducto(2L, "Producto 2", "Marca", "Modelo", gpuCategory, 200.0, 5, 2L);
        
        when(productRepository.findBySellerId(2L)).thenReturn(Arrays.asList(p1, p2));

        // CUANDO: buscamos por vendedor
        List<Product> resultado = productService.findBySellerId(2L);

        // ENTONCES: retorna 2 productos
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindBySellerId_VendedorSinProductos() {
        // DADO: un vendedor sin productos
        when(productRepository.findBySellerId(999L)).thenReturn(Arrays.asList());

        // CUANDO: buscamos por vendedor
        List<Product> resultado = productService.findBySellerId(999L);

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }
}
