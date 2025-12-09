package com.Resenas.Calificaciones.service;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review save(Review review) {
        // Validación de rating
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("La calificación (rating) debe ser un número entre 1 y 5.");
        }

        // Upsert: actualizar si existe, crear si no
        Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(review.getUserId(), review.getProductId());

        if (existingReview.isPresent()) {
            Review reviewToUpdate = existingReview.get();
            reviewToUpdate.setRating(review.getRating());
            reviewToUpdate.setComment(review.getComment());
            reviewToUpdate.setDate(LocalDate.now());
            return reviewRepository.save(reviewToUpdate);
        } else {
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

    /**
     * Calcula el rating promedio de un producto.
     */
    public Double getAverageRating(Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) {
            return null;
        }
        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    /**
     * Obtiene todas las reseñas de un usuario.
     */
    public List<Review> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId);
    }
}