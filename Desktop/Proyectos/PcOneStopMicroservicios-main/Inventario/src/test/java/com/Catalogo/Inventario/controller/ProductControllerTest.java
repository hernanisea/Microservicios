package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.model.Category;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category gpuCategory;
    private Product producto1;
    private Product producto2;

    @BeforeEach
    void setUp() {
        gpuCategory = new Category(1L, "GPU", "Tarjetas gráficas", null);
        producto1 = new Product();
        producto1.setId(1L);
        producto1.setName("RTX 4070");
        producto1.setBrand("Nvidia");
        producto1.setModel("RTX 4070");
        producto1.setCategoryEntity(gpuCategory);
        producto1.setPrice(700.0);
        producto1.setStock(10);
        producto1.setSellerId(1L);
        producto1.setDescription("Tarjeta gráfica potente");
        producto1.setImageUrl("https://example.com/rtx4070.jpg");
        producto2 = new Product();
        producto2.setId(2L);
        producto2.setName("Ryzen 7");
        producto2.setBrand("AMD");
        producto2.setModel("7800X3D");
        producto2.setCategoryEntity(gpuCategory);
        producto2.setPrice(400.0);
        producto2.setStock(5);
        producto2.setSellerId(1L);
    }

    // Tests GET /api/v1/products 
    @Test
    public void testList_RetornaProductos() throws Exception {
        // DADO: 2 productos
        List<Product> productos = Arrays.asList(producto1, producto2);
        when(productService.findAll()).thenReturn(productos);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/products"))
                // ENTONCES: respuesta 200 OK con 2 productos
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.count").value(2L));
    }

    @Test
    public void testList_SinProductos_Retorna204() throws Exception {
        // DADO: no hay productos
        when(productService.findAll()).thenReturn(Arrays.asList());

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/products"))
                // ENTONCES: respuesta 204 NO CONTENT
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(204))
                .andExpect(jsonPath("$.message").value("No hay productos registrados"));
    }

    // Tests GET /api/v1/products/{id} 
    @Test
    public void testGetById_ProductoExiste() throws Exception {
        // DADO: producto con ID 1
        when(productService.findById(1L)).thenReturn(producto1);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/products/1"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("RTX 4070"));
    }

    @Test
    public void testGetById_ProductoNoExiste_Retorna404() throws Exception {
        // DADO: producto que no existe
        when(productService.findById(999L))
                .thenThrow(new RuntimeException("Producto no encontrado con ID: 999"));

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/products/999"))
                // ENTONCES: respuesta 404 NOT FOUND
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    // Tests POST /api/v1/products 
    @Test
    public void testSave_CreaProductoExitosamente() throws Exception {
        // DADO: un producto nuevo
        Product nuevo = new Product();
        nuevo.setName("SSD Samsung");
        nuevo.setBrand("Samsung");
        nuevo.setModel("980 Pro");
        nuevo.setCategory("GPU");
        nuevo.setPrice(200.0);
        nuevo.setStock(15);
        nuevo.setSellerId(1L);
        
        Product guardado = new Product();
        guardado.setId(3L);
        guardado.setName("SSD Samsung");
        guardado.setBrand("Samsung");
        guardado.setModel("980 Pro");
        guardado.setCategoryEntity(gpuCategory);
        guardado.setPrice(200.0);
        guardado.setStock(15);
        guardado.setSellerId(1L);
        when(productService.save(any(Product.class))).thenReturn(guardado);

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                // ENTONCES: respuesta 201 CREATED
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.id").value(3L))
                .andExpect(jsonPath("$.data.name").value("SSD Samsung"));
    }

    @Test
    public void testSave_CategoriaNoExiste_Retorna400() throws Exception {
        // DADO: producto con categoría inexistente
        Product nuevo = new Product();
        nuevo.setName("Test");
        nuevo.setBrand("Test");
        nuevo.setModel("Test");
        nuevo.setCategory("INEXISTENTE");
        nuevo.setPrice(100.0);
        nuevo.setStock(10);
        nuevo.setSellerId(1L);
        
        when(productService.save(any(Product.class)))
                .thenThrow(new RuntimeException("Categoría no encontrada: INEXISTENTE"));

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                // ENTONCES: respuesta 400 BAD REQUEST
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    // Tests PUT /api/v1/products/{id} 
    @Test
    public void testUpdate_ActualizaProductoExitosamente() throws Exception {
        // DADO: producto a actualizar (con todos los campos requeridos para validación)
        Product actualizado = new Product();
        actualizado.setName("RTX 4070 Super");
        actualizado.setBrand("Nvidia");
        actualizado.setModel("RTX 4070 Super");
        actualizado.setPrice(750.0);
        actualizado.setStock(20);
        actualizado.setSellerId(1L);
        
        Product resultado = new Product();
        resultado.setId(1L);
        resultado.setName("RTX 4070 Super");
        resultado.setBrand("Nvidia");
        resultado.setModel("RTX 4070 Super");
        resultado.setCategoryEntity(gpuCategory);
        resultado.setPrice(750.0);
        resultado.setStock(20);
        resultado.setSellerId(1L);
        when(productService.update(1L, actualizado)).thenReturn(resultado);

        // CUANDO: enviamos PUT
        mockMvc.perform(put("/api/v1/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.name").value("RTX 4070 Super"))
                .andExpect(jsonPath("$.data.price").value(750.0));
    }

    @Test
    public void testUpdate_ProductoNoExiste_Retorna404() throws Exception {
        // DADO: producto que no existe (con todos los campos requeridos para validación)
        Product actualizado = new Product();
        actualizado.setName("Test");
        actualizado.setBrand("Test");
        actualizado.setModel("Test");
        actualizado.setPrice(100.0);
        actualizado.setStock(10);
        actualizado.setSellerId(1L);
        
        when(productService.update(999L, actualizado))
                .thenThrow(new RuntimeException("Producto no encontrado con ID: 999"));

        // CUANDO: enviamos PUT
        mockMvc.perform(put("/api/v1/products/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                // ENTONCES: respuesta 404 NOT FOUND
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ok").value(false));
    }

    // Tests PUT /api/v1/products/{id}/stock 
    @Test
    public void testReduceStock_ActualizaStockExitosamente() throws Exception {
        // DADO: producto con stock suficiente
        Product actualizado = new Product();
        actualizado.setId(1L);
        actualizado.setName("RTX 4070");
        actualizado.setBrand("Nvidia");
        actualizado.setModel("RTX 4070");
        actualizado.setCategoryEntity(gpuCategory);
        actualizado.setPrice(700.0);
        actualizado.setStock(7);
        actualizado.setSellerId(1L);
        when(productService.reduceStock(1L, 3)).thenReturn(actualizado);

        // CUANDO: enviamos PUT
        mockMvc.perform(put("/api/v1/products/1/stock")
                .param("quantity", "3"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.stock").value(7));
    }

    @Test
    public void testReduceStock_StockInsuficiente_Retorna400() throws Exception {
        // DADO: stock insuficiente
        when(productService.reduceStock(1L, 100))
                .thenThrow(new RuntimeException("Stock insuficiente"));

        // CUANDO: enviamos PUT
        mockMvc.perform(put("/api/v1/products/1/stock")
                .param("quantity", "100"))
                // ENTONCES: respuesta 400 BAD REQUEST
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false));
    }

    // Tests PUT /api/v1/products/{id}/stock/add 
    @Test
    public void testAddStock_IncrementaStockExitosamente() throws Exception {
        // DADO: producto al que se agrega stock
        Product actualizado = new Product();
        actualizado.setId(1L);
        actualizado.setName("RTX 4070");
        actualizado.setBrand("Nvidia");
        actualizado.setModel("RTX 4070");
        actualizado.setCategoryEntity(gpuCategory);
        actualizado.setPrice(700.0);
        actualizado.setStock(15);
        actualizado.setSellerId(1L);
        when(productService.addStock(1L, 5)).thenReturn(actualizado);

        // CUANDO: enviamos PUT
        mockMvc.perform(put("/api/v1/products/1/stock/add")
                .param("quantity", "5"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.stock").value(15));
    }

    // Tests DELETE /api/v1/products/{id} 
    @Test
    public void testDelete_EliminaProductoExitosamente() throws Exception {
        // DADO: producto a eliminar
        doNothing().when(productService).deleteProduct(1L);

        // CUANDO: enviamos DELETE
        mockMvc.perform(delete("/api/v1/products/1"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("Producto eliminado exitosamente"));
        
        verify(productService).deleteProduct(1L);
    }

    // Tests GET /api/v1/products/categories 
    @Test
    public void testGetAllCategories_RetornaCategorias() throws Exception {
        // DADO: 2 categorías
        Category cpu = new Category(2L, "CPU", "Procesadores", null);
        List<Category> categorias = Arrays.asList(gpuCategory, cpu);
        when(productService.findAllCategories()).thenReturn(categorias);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/products/categories"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    // Tests GET /api/v1/products/category/{categoryName} 
    @Test
    public void testGetByCategory_RetornaProductosDeLaCategoria() throws Exception {
        // DADO: productos de categoría GPU
        List<Product> productos = Arrays.asList(producto1, producto2);
        when(productService.findByCategory("GPU")).thenReturn(productos);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/products/category/GPU"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    public void testGetByCategory_CategoriaNoExiste_Retorna404() throws Exception {
        // DADO: categoría inexistente
        when(productService.findByCategory("INEXISTENTE"))
                .thenThrow(new RuntimeException("Categoría no encontrada: INEXISTENTE"));

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/products/category/INEXISTENTE"))
                // ENTONCES: respuesta 404 NOT FOUND
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ok").value(false));
    }

    // Tests GET /api/v1/products/seller/{sellerId} 
    @Test
    public void testGetBySeller_RetornaProductosDelVendedor() throws Exception {
        // DADO: productos del vendedor 1
        List<Product> productos = Arrays.asList(producto1, producto2);
        when(productService.findBySellerId(1L)).thenReturn(productos);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/products/seller/1"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }
}
