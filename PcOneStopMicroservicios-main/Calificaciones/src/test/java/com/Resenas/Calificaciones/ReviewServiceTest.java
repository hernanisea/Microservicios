package com.Resenas.Calificaciones;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import com.Resenas.Calificaciones.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Test
    public void testSaveReviewSuccess() {
        // Datos de prueba válidos
        Review review = new Review();
        review.setProductId(1L);
        review.setUserId(10L);
        review.setRating(5); // Rating válido
        review.setComment("Excelente");

        // Configurar Mock
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // Ejecutar
        Review saved = reviewService.save(review);

        // Validar
        assertNotNull(saved);
        assertEquals(5, saved.getRating());
        verify(reviewRepository, times(1)).save(review);
    }

    @Test
    public void testSaveReviewInvalidRating() {
        // Datos de prueba inválidos (Rating 6)
        Review review = new Review();
        review.setProductId(1L);
        review.setUserId(10L);
        review.setRating(6); // ¡Inválido!
        review.setComment("Fake");

        // Ejecutar y esperar excepción
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.save(review);
        });

        // Validar mensaje de error
        assertTrue(exception.getMessage().contains("entre 1 y 5"));
        
        // Asegurar que NUNCA se llamó al repositorio
        verify(reviewRepository, never()).save(any(Review.class));
    }
    
    @Test
    public void testFindByProductId() {
        Long prodId = 50L;
        when(reviewRepository.findByProductId(prodId)).thenReturn(List.of(new Review(), new Review()));
        
        List<Review> results = reviewService.findByProductId(prodId);
        
        assertNotNull(results);
        assertEquals(2, results.size());
    }
}