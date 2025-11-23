package com.Resenas.Calificaciones.service;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate; // <--- Asegúrate de tener este import

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review save(Review review) {
        // 1. Validación básica
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("La calificación (rating) debe ser un número entre 1 y 5.");
        }

        // 2. LOGICA ANTI-DUPLICADOS (Upsert)
        // Buscamos si este usuario ya calificó este producto
        Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(review.getUserId(), review.getProductId());

        if (existingReview.isPresent()) {
            // --- CASO: YA EXISTE -> ACTUALIZAR ---
            Review reviewToUpdate = existingReview.get();
            
            // Actualizamos solo los campos cambiantes
            reviewToUpdate.setRating(review.getRating());
            reviewToUpdate.setComment(review.getComment());
            reviewToUpdate.setDate(LocalDate.now()); // Actualizamos la fecha a hoy
            
            return reviewRepository.save(reviewToUpdate);
        } else {
            // --- CASO: NO EXISTE -> CREAR NUEVO ---
            review.setDate(LocalDate.now());
            return reviewRepository.save(review);
        }
    }

    public List<Review> findByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
    
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }
}