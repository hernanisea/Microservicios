package com.Resenas.Calificaciones.service;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
@Transactional
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private EntityManager entityManager;

    public Review save(Review review) {
        logger.info("Intentando guardar reseña: userId={}, productId={}, rating={}", 
                   review.getUserId(), review.getProductId(), review.getRating());
        
        // 1. Validación básica
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            logger.warn("Rating inválido: {}", review.getRating());
            throw new IllegalArgumentException("La calificación (rating) debe ser un número entre 1 y 5.");
        }

        // 2. LOGICA ANTI-DUPLICADOS (Upsert)
        // Buscamos si este usuario ya calificó este producto
        Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(review.getUserId(), review.getProductId());

        if (existingReview.isPresent()) {
            // --- CASO: YA EXISTE -> ACTUALIZAR ---
            Review reviewToUpdate = existingReview.get();
            logger.info("Reseña existente encontrada (ID: {}), actualizando...", reviewToUpdate.getId());
            
            // Actualizamos solo los campos cambiantes
            reviewToUpdate.setRating(review.getRating());
            reviewToUpdate.setComment(review.getComment());
            reviewToUpdate.setDate(LocalDate.now()); // Actualizamos la fecha a hoy
            
            Review saved = reviewRepository.save(reviewToUpdate);
            entityManager.flush(); // Forzar el flush para asegurar que se persista
            logger.info("Reseña actualizada exitosamente (ID: {})", saved.getId());
            return saved;
        } else {
            // --- CASO: NO EXISTE -> CREAR NUEVO ---
            logger.info("Creando nueva reseña...");
            review.setDate(LocalDate.now());
            Review saved = reviewRepository.save(review);
            entityManager.flush(); // Forzar el flush para asegurar que se persista
            logger.info("Reseña creada exitosamente (ID: {})", saved.getId());
            return saved;
        }
    }

    public List<Review> findByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
    
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }
}