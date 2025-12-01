package com.Resenas.Calificaciones.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EntityManager entityManager;

    // ==================== TESTS PARA save() - VALIDACIÓN ====================

    @Test
    public void testSave_RatingNulo_LanzaExcepcion() {
        // DADO: una reseña sin rating
        Review review = new Review();
        review.setRating(null);
        review.setProductId(1L);
        review.setUserId(1L);

        // CUANDO/ENTONCES: lanza IllegalArgumentException
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.save(review);
        });
        assertTrue(ex.getMessage().contains("entre 1 y 5"));
    }

    @Test
    public void testSave_RatingMenorA1_LanzaExcepcion() {
        // DADO: una reseña con rating 0
        Review review = new Review();
        review.setRating(0);

        // CUANDO/ENTONCES: lanza IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.save(review);
        });
    }

    @Test
    public void testSave_RatingMayorA5_LanzaExcepcion() {
        // DADO: una reseña con rating 6
        Review review = new Review();
        review.setRating(6);

        // CUANDO/ENTONCES: lanza IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.save(review);
        });
    }

    // ==================== TESTS PARA save() - CREAR NUEVA ====================

    @Test
    public void testSave_CreaNuevaResenaSiNoExiste() {
        // DADO: no hay reseña previa de este usuario para este producto
        when(reviewRepository.findByUserIdAndProductId(5L, 10L)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = (Review) i.getArguments()[0];
            r.setId(1L);
            return r;
        });

        Review nueva = new Review(null, 10L, 5L, 4, "Buen producto", null);

        // CUANDO: guardamos
        Review resultado = reviewService.save(nueva);

        // ENTONCES: se crea nueva y tiene fecha de hoy
        assertNotNull(resultado.getId());
        assertEquals(4, resultado.getRating());
        assertEquals(LocalDate.now(), resultado.getDate());
    }

    @Test
    public void testSave_RatingValido_EntreLimites() {
        // DADO: ratings válidos (1 y 5)
        when(reviewRepository.findByUserIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        // Rating 1 (mínimo válido)
        Review r1 = new Review(null, 1L, 1L, 1, "Malo", null);
        Review res1 = reviewService.save(r1);
        assertEquals(1, res1.getRating());

        // Rating 5 (máximo válido)
        Review r5 = new Review(null, 2L, 2L, 5, "Excelente", null);
        Review res5 = reviewService.save(r5);
        assertEquals(5, res5.getRating());
    }

    // ==================== TESTS PARA save() - ACTUALIZAR EXISTENTE ====================

    @Test
    public void testSave_ActualizaResenaSiYaExiste() {
        // DADO: ya existe una reseña del usuario 5 para producto 10
        Review existente = new Review(1L, 10L, 5L, 2, "Regular", LocalDate.of(2024, 1, 1));
        when(reviewRepository.findByUserIdAndProductId(5L, 10L)).thenReturn(Optional.of(existente));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        // Nueva reseña (mismo usuario y producto, diferente rating)
        Review nueva = new Review(null, 10L, 5L, 5, "Ahora me encanta!", null);

        // CUANDO: guardamos
        Review resultado = reviewService.save(nueva);

        // ENTONCES: actualiza la existente (mismo ID)
        assertEquals(1L, resultado.getId());
        assertEquals(5, resultado.getRating());
        assertEquals("Ahora me encanta!", resultado.getComment());
        assertEquals(LocalDate.now(), resultado.getDate()); // Fecha actualizada
    }

    @Test
    public void testSave_ActualizaMantieneIdOriginal() {
        // DADO: una reseña existente con ID 99
        Review existente = new Review(99L, 10L, 5L, 3, "Ok", LocalDate.of(2024, 6, 15));
        when(reviewRepository.findByUserIdAndProductId(5L, 10L)).thenReturn(Optional.of(existente));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        Review nueva = new Review(null, 10L, 5L, 4, "Mejor de lo esperado", null);

        // CUANDO: guardamos
        Review resultado = reviewService.save(nueva);

        // ENTONCES: mantiene el ID original 99
        assertEquals(99L, resultado.getId());
    }

    // ==================== TESTS PARA findByProductId() ====================

    @Test
    public void testFindByProductId_RetornaResenasDelProducto() {
        // DADO: 2 reseñas para el producto 10
        Review r1 = new Review(1L, 10L, 1L, 5, "Excelente", LocalDate.now());
        Review r2 = new Review(2L, 10L, 2L, 4, "Muy bueno", LocalDate.now());
        when(reviewRepository.findByProductId(10L)).thenReturn(Arrays.asList(r1, r2));

        // CUANDO: buscamos por productId
        List<Review> resultado = reviewService.findByProductId(10L);

        // ENTONCES: retorna 2 reseñas
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindByProductId_ProductoSinResenas() {
        // DADO: producto sin reseñas
        when(reviewRepository.findByProductId(999L)).thenReturn(Arrays.asList());

        // CUANDO: buscamos por productId
        List<Review> resultado = reviewService.findByProductId(999L);

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS PARA findAll() ====================

    @Test
    public void testFindAll_RetornaTodasLasResenas() {
        // DADO: 3 reseñas totales
        Review r1 = new Review(1L, 1L, 1L, 5, "A", LocalDate.now());
        Review r2 = new Review(2L, 2L, 2L, 4, "B", LocalDate.now());
        Review r3 = new Review(3L, 3L, 3L, 3, "C", LocalDate.now());
        when(reviewRepository.findAll()).thenReturn(Arrays.asList(r1, r2, r3));

        // CUANDO: obtenemos todas
        List<Review> resultado = reviewService.findAll();

        // ENTONCES: retorna 3
        assertEquals(3, resultado.size());
    }

    @Test
    public void testFindAll_SinResenas() {
        // DADO: no hay reseñas
        when(reviewRepository.findAll()).thenReturn(Arrays.asList());

        // CUANDO: obtenemos todas
        List<Review> resultado = reviewService.findAll();

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }
}
