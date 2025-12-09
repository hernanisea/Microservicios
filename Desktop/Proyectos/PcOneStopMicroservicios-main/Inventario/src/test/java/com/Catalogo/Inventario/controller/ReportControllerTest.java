package com.Catalogo.Inventario.controller;

import com.Catalogo.Inventario.model.ProductReport;
import com.Catalogo.Inventario.repository.ReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportRepository reportRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ProductReport reporte1;
    private ProductReport reporte2;

    @BeforeEach
    void setUp() {
        reporte1 = new ProductReport(1L, 1L, 4L, "El producto no coincide con la descripción", LocalDate.now());
        reporte2 = new ProductReport(2L, 1L, 5L, "Producto dañado", LocalDate.now());
    }

    // Tests POST /api/v1/reports 
    @Test
    public void testCreateReport_CreaReporteExitosamente() throws Exception {
        // DADO: un reporte nuevo
        ProductReport nuevo = new ProductReport(null, 1L, 4L, "El producto no coincide con la descripción", null);
        ProductReport guardado = new ProductReport(3L, 1L, 4L, "El producto no coincide con la descripción", LocalDate.now());
        
        when(reportRepository.save(any(ProductReport.class))).thenReturn(guardado);

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                // ENTONCES: respuesta 201 CREATED
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Reporte enviado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(3L))
                .andExpect(jsonPath("$.data.productId").value(1L))
                .andExpect(jsonPath("$.data.userId").value(4L));
    }

    @Test
    public void testCreateReport_ErrorInterno_Retorna500() throws Exception {
        // DADO: error al guardar
        ProductReport nuevo = new ProductReport(null, 1L, 4L, "Test", null);
        
        when(reportRepository.save(any(ProductReport.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevo)))
                // ENTONCES: respuesta 500 INTERNAL SERVER ERROR
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(500));
    }

    // Tests GET /api/v1/reports/count/{productId} 
    @Test
    public void testGetReportCount_ProductoConReportes_RetornaCantidad() throws Exception {
        // DADO: producto con 2 reportes
        when(reportRepository.countByProductId(1L)).thenReturn(2L);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reports/count/1"))
                // ENTONCES: respuesta 200 OK con cantidad
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(2L))
                .andExpect(jsonPath("$.message").value("El producto tiene 2 reporte(s)"));
    }

    @Test
    public void testGetReportCount_ProductoSinReportes_RetornaCero() throws Exception {
        // DADO: producto sin reportes
        when(reportRepository.countByProductId(999L)).thenReturn(0L);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reports/count/999"))
                // ENTONCES: respuesta 200 OK con 0
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(0L))
                .andExpect(jsonPath("$.message").value("El producto no tiene reportes"));
    }

    // Tests GET /api/v1/reports 
    @Test
    public void testGetAllReports_RetornaTodosLosReportes() throws Exception {
        // DADO: 2 reportes en total
        List<ProductReport> reportes = Arrays.asList(reporte1, reporte2);
        when(reportRepository.findAll()).thenReturn(reportes);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reports"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.count").value(2L));
    }

    @Test
    public void testGetAllReports_SinReportes_RetornaListaVacia() throws Exception {
        // DADO: no hay reportes
        when(reportRepository.findAll()).thenReturn(Arrays.asList());

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reports"))
                // ENTONCES: respuesta 200 OK con lista vacía
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.count").value(0L));
    }

    // Tests GET /api/v1/reports/product/{productId} 
    @Test
    public void testGetReportsByProduct_RetornaReportesDelProducto() throws Exception {
        // DADO: 2 reportes para el producto 1
        List<ProductReport> reportes = Arrays.asList(reporte1, reporte2);
        when(reportRepository.findByProductId(1L)).thenReturn(reportes);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reports/product/1"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.count").value(2L))
                .andExpect(jsonPath("$.message").value("Reportes del producto"));
    }

    @Test
    public void testGetReportsByProduct_ProductoSinReportes_RetornaListaVacia() throws Exception {
        // DADO: producto sin reportes
        when(reportRepository.findByProductId(999L)).thenReturn(Arrays.asList());

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reports/product/999"))
                // ENTONCES: respuesta 200 OK con lista vacía
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.count").value(0L));
    }
}

