package com.Resenas.Calificaciones.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Resenas.Calificaciones.dto.ReviewRequest;
import com.Resenas.Calificaciones.dto.ReviewResponse;
import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    // Helper para crear reseñas de prueba
    private Review crearReview(String id, String productId, String userId, String author, 
                               Integer rating, String comment) {
        Review r = new Review();
        r.setId(id);
        r.setProductId(productId);
        r.setUserId(userId);
        r.setAuthor(author);
        r.setRating(rating);
        r.setComment(comment);
        r.setDate(LocalDateTime.now().toString());
        return r;
    }

    // ==================== TESTS PARA save() - VALIDACIÓN ====================

    @Test
    public void testSave_RatingNulo_LanzaExcepcion() {
        // DADO: una reseña sin rating
        Review review = new Review();
        review.setRating(null);
        review.setProductId("cpu-ryzen-5600");
        review.setUserId("user-client-01");
        review.setAuthor("Pedro");

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
        review.setProductId("cpu-ryzen-5600");
        review.setAuthor("Pedro");

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
        review.setProductId("cpu-ryzen-5600");
        review.setAuthor("Pedro");

        // CUANDO/ENTONCES: lanza IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.save(review);
        });
    }

    // ==================== TESTS PARA save() - CREAR NUEVA ====================

    @Test
    public void testSave_CreaNuevaResenaSiNoExiste() {
        // DADO: no hay reseña previa de este usuario para este producto
        when(reviewRepository.findByUserIdAndProductId("user-client-01", "cpu-ryzen-5600"))
            .thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = (Review) i.getArguments()[0];
            if (r.getId() == null || r.getId().isEmpty()) {
                r.setId("review-abc123");
            }
            return r;
        });

        Review nueva = new Review();
        nueva.setProductId("cpu-ryzen-5600");
        nueva.setUserId("user-client-01");
        nueva.setAuthor("Pedro");
        nueva.setRating(4);
        nueva.setComment("Buen producto");

        // CUANDO: guardamos
        Review resultado = reviewService.save(nueva);

        // ENTONCES: se crea nueva y tiene ID generado
        assertNotNull(resultado.getId());
        assertEquals(4, resultado.getRating());
        assertNotNull(resultado.getDate());
    }

    @Test
    public void testSave_GeneraAutorAnonimoSiNoViene() {
        // DADO: una reseña sin author ni userId (anónima)
        // Cuando userId es null, el servicio NO llama a findByUserIdAndProductId
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        Review nueva = new Review();
        nueva.setProductId("gpu-rtx-4060");
        nueva.setRating(5);
        nueva.setComment("Excelente!");
        // Sin author ni userId

        // CUANDO: guardamos
        Review resultado = reviewService.save(nueva);

        // ENTONCES: author es "Anónimo"
        assertEquals("Anónimo", resultado.getAuthor());
    }

    @Test
    public void testSave_RatingValido_EntreLimites() {
        // DADO: ratings válidos (1 y 5)
        when(reviewRepository.findByUserIdAndProductId(anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        // Rating 1 (mínimo válido)
        Review r1 = crearReview(null, "prod-1", "user-1", "Juan", 1, "Malo");
        Review res1 = reviewService.save(r1);
        assertEquals(1, res1.getRating());

        // Rating 5 (máximo válido)
        Review r5 = crearReview(null, "prod-2", "user-2", "Ana", 5, "Excelente");
        Review res5 = reviewService.save(r5);
        assertEquals(5, res5.getRating());
    }

    // ==================== TESTS PARA save() - ACTUALIZAR EXISTENTE ====================

    @Test
    public void testSave_ActualizaResenaSiYaExiste() {
        // DADO: ya existe una reseña del usuario para este producto
        Review existente = crearReview("review-001", "cpu-ryzen-5600", "user-client-01", 
                                       "Pedro", 2, "Regular");
        when(reviewRepository.findByUserIdAndProductId("user-client-01", "cpu-ryzen-5600"))
            .thenReturn(Optional.of(existente));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        // Nueva reseña (mismo usuario y producto, diferente rating)
        Review nueva = new Review();
        nueva.setProductId("cpu-ryzen-5600");
        nueva.setUserId("user-client-01");
        nueva.setAuthor("Pedro");
        nueva.setRating(5);
        nueva.setComment("Ahora me encanta!");

        // CUANDO: guardamos
        Review resultado = reviewService.save(nueva);

        // ENTONCES: actualiza la existente (mismo ID)
        assertEquals("review-001", resultado.getId());
        assertEquals(5, resultado.getRating());
        assertEquals("Ahora me encanta!", resultado.getComment());
    }

    // ==================== TESTS PARA findByProductId() ====================

    @Test
    public void testFindByProductId_RetornaResenasDelProducto() {
        // DADO: 2 reseñas para el producto
        Review r1 = crearReview("review-001", "cpu-ryzen-5600", "user-1", "Juan", 5, "Excelente");
        Review r2 = crearReview("review-002", "cpu-ryzen-5600", "user-2", "Ana", 4, "Muy bueno");
        when(reviewRepository.findByProductId("cpu-ryzen-5600")).thenReturn(Arrays.asList(r1, r2));

        // CUANDO: buscamos por productId
        List<Review> resultado = reviewService.findByProductId("cpu-ryzen-5600");

        // ENTONCES: retorna 2 reseñas
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindByProductId_ProductoSinResenas() {
        // DADO: producto sin reseñas
        when(reviewRepository.findByProductId("producto-nuevo")).thenReturn(Arrays.asList());

        // CUANDO: buscamos por productId
        List<Review> resultado = reviewService.findByProductId("producto-nuevo");

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS PARA findByUserId() ====================

    @Test
    public void testFindByUserId_RetornaResenasDelUsuario() {
        // DADO: 2 reseñas del mismo usuario
        Review r1 = crearReview("review-001", "cpu-ryzen-5600", "user-client-01", "Pedro", 5, "A");
        Review r2 = crearReview("review-002", "gpu-rtx-4060", "user-client-01", "Pedro", 4, "B");
        when(reviewRepository.findByUserId("user-client-01")).thenReturn(Arrays.asList(r1, r2));

        // CUANDO: buscamos por userId
        List<Review> resultado = reviewService.findByUserId("user-client-01");

        // ENTONCES: retorna 2 reseñas
        assertEquals(2, resultado.size());
    }

    // ==================== TESTS PARA findAll() ====================

    @Test
    public void testFindAll_RetornaTodasLasResenas() {
        // DADO: 3 reseñas totales
        Review r1 = crearReview("review-001", "prod-1", "user-1", "Juan", 5, "A");
        Review r2 = crearReview("review-002", "prod-2", "user-2", "Ana", 4, "B");
        Review r3 = crearReview("review-003", "prod-3", "user-3", "Pedro", 3, "C");
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

    // ==================== TESTS PARA deleteReview() ====================

    @Test
    public void testDeleteReview_EliminaCorrectamente() {
        // CUANDO: eliminamos reseña
        reviewService.deleteReview("review-001");

        // ENTONCES: se llama al repositorio
        verify(reviewRepository).deleteById("review-001");
    }

    // ==================== TESTS PARA MÉTODOS CON DTOs ====================

    // ==================== TESTS PARA getReviewsByProductId() ====================

    @Test
    public void testGetReviewsByProductId_RetornaListaDeReviewResponse() {
        // DADO: 2 reseñas para el producto ordenadas por fecha descendente
        Review r1 = crearReview("review-001", "cpu-ryzen-5600", "user-1", "Juan", 5, "Excelente");
        Review r2 = crearReview("review-002", "cpu-ryzen-5600", "user-2", "Ana", 4, "Muy bueno");
        when(reviewRepository.findByProductIdOrderByDateDesc("cpu-ryzen-5600"))
            .thenReturn(Arrays.asList(r1, r2));

        // CUANDO: obtenemos reseñas usando DTOs
        List<ReviewResponse> resultado = reviewService.getReviewsByProductId("cpu-ryzen-5600");

        // ENTONCES: retorna 2 ReviewResponse
        assertEquals(2, resultado.size());
        assertEquals("review-001", resultado.get(0).getId());
        assertEquals("Juan", resultado.get(0).getAuthor());
        assertEquals(5, resultado.get(0).getRating());
    }

    @Test
    public void testGetReviewsByProductId_ListaVacia() {
        // DADO: producto sin reseñas
        when(reviewRepository.findByProductIdOrderByDateDesc("producto-nuevo"))
            .thenReturn(Arrays.asList());

        // CUANDO: obtenemos reseñas
        List<ReviewResponse> resultado = reviewService.getReviewsByProductId("producto-nuevo");

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS PARA createReview() ====================

    @Test
    public void testCreateReview_CreaNuevaResenaConDTO() {
        // DADO: un ReviewRequest válido
        ReviewRequest request = new ReviewRequest();
        request.setProductId("cpu-ryzen-5600");
        request.setUserId("user-client-01");
        request.setAuthor("Pedro");
        request.setRating(4);
        request.setComment("Buen producto");

        Review reviewGuardado = crearReview("review-abc123", "cpu-ryzen-5600", 
            "user-client-01", "Pedro", 4, "Buen producto");
        
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewGuardado);

        // CUANDO: creamos reseña usando DTO
        ReviewResponse resultado = reviewService.createReview(request);

        // ENTONCES: retorna ReviewResponse con datos correctos
        assertNotNull(resultado.getId());
        assertEquals("cpu-ryzen-5600", resultado.getProductId());
        assertEquals("user-client-01", resultado.getUserId());
        assertEquals("Pedro", resultado.getAuthor());
        assertEquals(4, resultado.getRating());
        assertEquals("Buen producto", resultado.getComment());
        assertNotNull(resultado.getDate());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    public void testCreateReview_GeneraAutorAnonimoSiNoViene() {
        // DADO: un ReviewRequest sin author ni userId
        ReviewRequest request = new ReviewRequest();
        request.setProductId("gpu-rtx-4060");
        request.setRating(5);
        request.setComment("Excelente!");

        Review reviewGuardado = crearReview("review-xyz789", "gpu-rtx-4060", 
            null, "Anónimo", 5, "Excelente!");
        
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewGuardado);

        // CUANDO: creamos reseña
        ReviewResponse resultado = reviewService.createReview(request);

        // ENTONCES: author es "Anónimo"
        assertEquals("Anónimo", resultado.getAuthor());
    }

    @Test
    public void testCreateReview_GeneraIdAutomaticamente() {
        // DADO: un ReviewRequest sin ID
        ReviewRequest request = new ReviewRequest();
        request.setProductId("cpu-ryzen-5600");
        request.setRating(5);
        request.setComment("Excelente!");

        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> {
            Review r = (Review) i.getArguments()[0];
            if (r.getId() == null || r.getId().isEmpty()) {
                r.setId("review-generado-123");
            }
            return r;
        });

        // CUANDO: creamos reseña
        ReviewResponse resultado = reviewService.createReview(request);

        // ENTONCES: tiene ID generado
        assertNotNull(resultado.getId());
        assertTrue(resultado.getId().startsWith("review-"));
    }

    @Test
    public void testCreateReview_RatingNulo_LanzaExcepcion() {
        // DADO: un ReviewRequest sin rating
        ReviewRequest request = new ReviewRequest();
        request.setProductId("cpu-ryzen-5600");
        request.setRating(null);
        request.setComment("Comentario");

        // CUANDO/ENTONCES: lanza IllegalArgumentException
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(request);
        });
        assertTrue(ex.getMessage().contains("entre 1 y 5"));
    }

    @Test
    public void testCreateReview_RatingInvalido_LanzaExcepcion() {
        // DADO: un ReviewRequest con rating inválido
        ReviewRequest request = new ReviewRequest();
        request.setProductId("cpu-ryzen-5600");
        request.setRating(6); // Rating mayor a 5
        request.setComment("Comentario");

        // CUANDO/ENTONCES: lanza IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.createReview(request);
        });
    }

    // ==================== TESTS PARA updateReview() ====================

    @Test
    public void testUpdateReview_ActualizaResenaExistente() {
        // DADO: una reseña existente
        Review existente = crearReview("review-001", "cpu-ryzen-5600", 
            "user-client-01", "Pedro", 2, "Regular");
        
        when(reviewRepository.findByIdAndProductId("review-001", "cpu-ryzen-5600"))
            .thenReturn(existente);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Ahora me encanta!");
        request.setAuthor("Pedro Actualizado");

        // CUANDO: actualizamos reseña
        ReviewResponse resultado = reviewService.updateReview("review-001", "cpu-ryzen-5600", request);

        // ENTONCES: se actualiza correctamente
        assertEquals("review-001", resultado.getId());
        assertEquals(5, resultado.getRating());
        assertEquals("Ahora me encanta!", resultado.getComment());
        assertEquals("Pedro Actualizado", resultado.getAuthor());
        verify(reviewRepository).save(existente);
    }

    @Test
    public void testUpdateReview_ResenaNoEncontrada_LanzaExcepcion() {
        // DADO: reseña que no existe
        when(reviewRepository.findByIdAndProductId("review-inexistente", "cpu-ryzen-5600"))
            .thenReturn(null);

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Comentario");

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            reviewService.updateReview("review-inexistente", "cpu-ryzen-5600", request);
        });
        assertTrue(ex.getMessage().contains("Reseña no encontrada"));
    }

    @Test
    public void testUpdateReview_NoActualizaAuthorSiNoVieneEnRequest() {
        // DADO: una reseña existente con author
        Review existente = crearReview("review-001", "cpu-ryzen-5600", 
            "user-client-01", "Pedro Original", 2, "Regular");
        
        when(reviewRepository.findByIdAndProductId("review-001", "cpu-ryzen-5600"))
            .thenReturn(existente);
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArguments()[0]);

        ReviewRequest request = new ReviewRequest();
        request.setRating(5);
        request.setComment("Nuevo comentario");
        // Sin author en el request

        // CUANDO: actualizamos reseña
        ReviewResponse resultado = reviewService.updateReview("review-001", "cpu-ryzen-5600", request);

        // ENTONCES: mantiene el author original
        assertEquals("Pedro Original", resultado.getAuthor());
        assertEquals(5, resultado.getRating());
    }

    @Test
    public void testUpdateReview_RatingInvalido_LanzaExcepcion() {
        // DADO: un ReviewRequest con rating inválido
        ReviewRequest request = new ReviewRequest();
        request.setRating(0); // Rating menor a 1
        request.setComment("Comentario");

        // CUANDO/ENTONCES: lanza IllegalArgumentException antes de buscar la reseña
        assertThrows(IllegalArgumentException.class, () -> {
            reviewService.updateReview("review-001", "cpu-ryzen-5600", request);
        });
    }

    // ==================== TESTS PARA deleteReview(String, String) ====================

    @Test
    public void testDeleteReview_ConProductId_EliminaCorrectamente() {
        // DADO: una reseña existente
        Review existente = crearReview("review-001", "cpu-ryzen-5600", 
            "user-client-01", "Pedro", 5, "Excelente");
        
        when(reviewRepository.findByIdAndProductId("review-001", "cpu-ryzen-5600"))
            .thenReturn(existente);

        // CUANDO: eliminamos reseña
        reviewService.deleteReview("review-001", "cpu-ryzen-5600");

        // ENTONCES: se elimina correctamente
        verify(reviewRepository).delete(existente);
    }

    @Test
    public void testDeleteReview_ResenaNoEncontrada_LanzaExcepcion() {
        // DADO: reseña que no existe
        when(reviewRepository.findByIdAndProductId("review-inexistente", "cpu-ryzen-5600"))
            .thenReturn(null);

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            reviewService.deleteReview("review-inexistente", "cpu-ryzen-5600");
        });
        assertTrue(ex.getMessage().contains("Reseña no encontrada"));
    }

    @Test
    public void testDeleteReview_ProductIdNoCoincide_LanzaExcepcion() {
        // DADO: reseña con productId diferente
        when(reviewRepository.findByIdAndProductId("review-001", "producto-diferente"))
            .thenReturn(null);

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            reviewService.deleteReview("review-001", "producto-diferente");
        });
        assertTrue(ex.getMessage().contains("Reseña no encontrada"));
    }
}
