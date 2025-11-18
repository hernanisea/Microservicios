package com.Resenas.Calificaciones.service;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review save(Review review) {
        // Validación de negocio: Rating debe estar entre 1 y 5
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("La calificación (rating) debe ser un número entre 1 y 5.");
        }
        return reviewRepository.save(review);
    }

    public List<Review> findByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
    
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }
}