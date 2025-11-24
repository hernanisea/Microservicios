package com.Resenas.Calificaciones.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Test
    public void testSaveReviewValidatesRating() {
        Review badReview = new Review();
        badReview.setRating(6); // Inválido

        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.save(badReview);
        });
    }

    @Test
    public void testSaveUpdatesExistingReview() {
        // Simular que el usuario YA TIENE una reseña (ID 1)
        Review existing = new Review(1L, 10L, 5L, 1, "Malo", null);
        when(reviewRepository.findByUserIdAndProductId(5L, 10L)).thenReturn(Optional.of(existing));
        
        // Nueva reseña (mismo usuario y producto)
        Review newReview = new Review(null, 10L, 5L, 5, "Bueno!", null);

        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        Review result = reviewService.save(newReview);

        // Debe haber actualizado el objeto existente, manteniendo el ID 1L
        assertEquals(1L, result.getId());
        assertEquals(5, result.getRating()); // Rating actualizado
        assertEquals("Bueno!", result.getComment());
    }

    @Test
    public void testSaveCreatesNewIfNotExist() {
        when(reviewRepository.findByUserIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        Review r = new Review(null, 10L, 5L, 5, "Ok", null);
        Review result = reviewService.save(r);

        assertEquals(5, result.getRating());
    }
}