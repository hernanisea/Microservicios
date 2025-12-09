package com.Resenas.Calificaciones.controller;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    // Tests POST /api/v1/reviews

    @Test
    public void testAdd_CreaResenaExitosamente() throws Exception {
        // DADO: una reseña válida
        Review nuevaReview = new Review(null, 1L, 4L, 5, "Excelente producto", null);
        Review savedReview = new Review(1L, 1L, 4L, 5, "Excelente producto", LocalDate.now());
        
        when(reviewService.save(any(Review.class))).thenReturn(savedReview);

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevaReview)))
                // ENTONCES: respuesta 201 CREATED
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Reseña guardada exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    public void testAdd_RatingInvalido_Retorna400() throws Exception {
        // DADO: una reseña con rating inválido (6)
        Review reviewInvalida = new Review(null, 1L, 4L, 6, "Test", null);
        
        when(reviewService.save(any(Review.class)))
                .thenThrow(new IllegalArgumentException("La calificación (rating) debe ser un número entre 1 y 5."));

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewInvalida)))
                // ENTONCES: respuesta 400 BAD REQUEST
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("La calificación (rating) debe ser un número entre 1 y 5."));
    }

    @Test
    public void testAdd_ErrorInterno_Retorna500() throws Exception {
        // DADO: un error interno
        Review review = new Review(null, 1L, 4L, 5, "Test", null);
        
        when(reviewService.save(any(Review.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(review)))
                // ENTONCES: respuesta 500 INTERNAL SERVER ERROR
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(500))
                .andExpect(jsonPath("$.message").value("Error al guardar reseña: Error de base de datos"));
    }

    // Tests GET /api/v1/reviews/product/{productId}

    @Test
    public void testGetByProduct_RetornaResenas() throws Exception {
        // DADO: 2 reseñas para el producto 1
        Review r1 = new Review(1L, 1L, 4L, 5, "Excelente", LocalDate.now());
        Review r2 = new Review(2L, 1L, 5L, 4, "Muy bueno", LocalDate.now());
        List<Review> reviews = Arrays.asList(r1, r2);
        
        when(reviewService.findByProductId(1L)).thenReturn(reviews);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reviews/product/1"))
                // ENTONCES: respuesta 200 OK con 2 reseñas
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.count").value(2L));
    }

    @Test
    public void testGetByProduct_SinResenas_Retorna204() throws Exception {
        // DADO: producto sin reseñas
        when(reviewService.findByProductId(999L)).thenReturn(Arrays.asList());

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reviews/product/999"))
                // ENTONCES: respuesta 204 NO CONTENT
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(204))
                .andExpect(jsonPath("$.message").value("Este producto aún no tiene reseñas"));
    }

    // Tests GET /api/v1/reviews/product/{productId}/average

    @Test
    public void testGetAverageRating_RetornaPromedio() throws Exception {
        // DADO: promedio de 4.5
        when(reviewService.getAverageRating(1L)).thenReturn(4.5);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reviews/product/1/average"))
                // ENTONCES: respuesta 200 OK con promedio
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").value(4.5))
                .andExpect(jsonPath("$.message").value("Rating promedio: 4.5 estrellas"))
                .andExpect(jsonPath("$.count").value(1L));
    }

    @Test
    public void testGetAverageRating_SinCalificaciones_RetornaNull() throws Exception {
        // DADO: producto sin calificaciones
        when(reviewService.getAverageRating(999L)).thenReturn(null);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reviews/product/999/average"))
                // ENTONCES: respuesta 200 OK con null (pero @JsonInclude.NON_NULL lo excluye del JSON)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").doesNotExist()) // Como es null, no se incluye en el JSON
                .andExpect(jsonPath("$.message").value("Este producto aún no tiene calificaciones"))
                .andExpect(jsonPath("$.count").value(1L));
    }

    // Tests GET /api/v1/reviews

    @Test
    public void testGetAll_RetornaTodasLasResenas() throws Exception {
        // DADO: 3 reseñas en total
        Review r1 = new Review(1L, 1L, 4L, 5, "A", LocalDate.now());
        Review r2 = new Review(2L, 2L, 5L, 4, "B", LocalDate.now());
        Review r3 = new Review(3L, 3L, 6L, 3, "C", LocalDate.now());
        List<Review> reviews = Arrays.asList(r1, r2, r3);
        
        when(reviewService.findAll()).thenReturn(reviews);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reviews"))
                // ENTONCES: respuesta 200 OK con 3 reseñas
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.count").value(3L));
    }

    // Tests GET /api/v1/reviews/user/{userId}

    @Test
    public void testGetByUser_RetornaResenasDelUsuario() throws Exception {
        // DADO: 2 reseñas del usuario 4
        Review r1 = new Review(1L, 1L, 4L, 5, "Producto A", LocalDate.now());
        Review r2 = new Review(2L, 2L, 4L, 4, "Producto B", LocalDate.now());
        List<Review> reviews = Arrays.asList(r1, r2);
        
        when(reviewService.findByUserId(4L)).thenReturn(reviews);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reviews/user/4"))
                // ENTONCES: respuesta 200 OK con 2 reseñas
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.count").value(2L));
    }

    @Test
    public void testGetByUser_UsuarioSinResenas_RetornaListaVacia() throws Exception {
        // DADO: usuario sin reseñas
        when(reviewService.findByUserId(999L)).thenReturn(Arrays.asList());

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/reviews/user/999"))
                // ENTONCES: respuesta 200 OK con lista vacía
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0))
                .andExpect(jsonPath("$.count").value(0L));
    }
}
