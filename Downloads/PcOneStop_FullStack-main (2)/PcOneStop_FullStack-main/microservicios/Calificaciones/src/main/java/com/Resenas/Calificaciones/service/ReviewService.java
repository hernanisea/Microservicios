package com.Resenas.Calificaciones.service;

import com.Resenas.Calificaciones.dto.ReviewRequest;
import com.Resenas.Calificaciones.dto.ReviewResponse;
import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;
    
    // Convertir Entity a Response
    private ReviewResponse toResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setProductId(review.getProductId());
        response.setUserId(review.getUserId());
        response.setAuthor(review.getAuthor());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setDate(review.getDate());
        return response;
    }
    
    // Convertir Request a Entity
    private Review toEntity(ReviewRequest request) {
        Review review = new Review();
        review.setId(request.getId());
        review.setProductId(request.getProductId());
        review.setUserId(request.getUserId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        // Si userId es null, el autor es "Anónimo"
        // Si userId existe, usar el author del request o "Usuario" por defecto
        if (request.getAuthor() != null && !request.getAuthor().isEmpty()) {
            review.setAuthor(request.getAuthor());
        } else if (request.getUserId() != null && !request.getUserId().isEmpty()) {
            review.setAuthor("Usuario");
        } else {
            review.setAuthor("Anónimo");
        }
        
        review.setDate(LocalDateTime.now().toString());
        return review;
    }

    // Método para CREAR (POST)
    public Review create(String productId, Review review) {
        // Asignar el producto de la URL al objeto
        review.setProductId(productId);

        // Validar rating
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("La calificación debe ser entre 1 y 5.");
        }

        // Generar ID si no existe
        if (review.getId() == null || review.getId().isEmpty()) {
            review.setId("review-" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        // Fecha actual
        review.setDate(LocalDateTime.now().toString());
        
        // Autor anónimo si falta
        if (review.getAuthor() == null || review.getAuthor().isEmpty()) {
            review.setAuthor("Anónimo");
        }

        return reviewRepository.save(review);
    }

    // Método para ACTUALIZAR (PUT)
    public Review update(String reviewId, Review reviewData) {
        Review existingReview = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        // Actualizamos solo los campos permitidos
        existingReview.setRating(reviewData.getRating());
        existingReview.setComment(reviewData.getComment());
        existingReview.setDate(LocalDateTime.now().toString()); // Actualizamos fecha

        return reviewRepository.save(existingReview);
    }

    // Método save() que crea o actualiza según si ya existe una reseña del usuario para el producto
    public Review save(Review review) {
        // Validar rating
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("La calificación debe ser entre 1 y 5.");
        }

        // Si tiene userId y productId, buscar si ya existe una reseña
        if (review.getUserId() != null && !review.getUserId().isEmpty() 
            && review.getProductId() != null && !review.getProductId().isEmpty()) {
            Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(
                review.getUserId(), review.getProductId());
            
            if (existingReview.isPresent()) {
                // Actualizar la reseña existente
                Review reviewToUpdate = existingReview.get();
                reviewToUpdate.setRating(review.getRating());
                reviewToUpdate.setComment(review.getComment());
                reviewToUpdate.setDate(LocalDateTime.now().toString());
                // Mantener el author original si no viene uno nuevo
                if (review.getAuthor() != null && !review.getAuthor().isEmpty()) {
                    reviewToUpdate.setAuthor(review.getAuthor());
                }
                return reviewRepository.save(reviewToUpdate);
            }
        }

        // Si no existe, crear nueva reseña
        // Generar ID si no existe
        if (review.getId() == null || review.getId().isEmpty()) {
            review.setId("review-" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        // Fecha actual
        review.setDate(LocalDateTime.now().toString());
        
        // Autor anónimo si falta
        if (review.getAuthor() == null || review.getAuthor().isEmpty()) {
            review.setAuthor("Anónimo");
        }

        return reviewRepository.save(review);
    }

    public List<Review> findByProductId(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    public List<Review> findByUserId(String userId) {
        return reviewRepository.findByUserId(userId);
    }

    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }
    
    // ========== MÉTODOS CON DTOs SEGÚN LA GUÍA ==========
    
    // Obtener todas las reseñas de un producto (usando DTOs)
    public List<ReviewResponse> getReviewsByProductId(String productId) {
        List<Review> reviews = reviewRepository.findByProductIdOrderByDateDesc(productId);
        return reviews.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    // Crear una nueva reseña (usando DTOs)
    public ReviewResponse createReview(ReviewRequest request) {
        // Validar rating antes de crear
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("La calificación debe ser entre 1 y 5.");
        }
        
        Review review = toEntity(request);
        
        // Generar ID si no existe
        if (review.getId() == null || review.getId().isEmpty()) {
            review.setId("review-" + UUID.randomUUID().toString().substring(0, 8));
        }
        
        Review savedReview = reviewRepository.save(review);
        return toResponse(savedReview);
    }
    
    // Actualizar una reseña existente (usando DTOs)
    public ReviewResponse updateReview(String reviewId, String productId, ReviewRequest request) {
        // Validar rating antes de actualizar
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("La calificación debe ser entre 1 y 5.");
        }
        
        Review review = reviewRepository.findByIdAndProductId(reviewId, productId);
        
        if (review == null) {
            throw new RuntimeException("Reseña no encontrada");
        }
        
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setDate(LocalDateTime.now().toString());
        
        // Actualizar author si viene en el request
        if (request.getAuthor() != null && !request.getAuthor().isEmpty()) {
            review.setAuthor(request.getAuthor());
        }
        
        Review updatedReview = reviewRepository.save(review);
        return toResponse(updatedReview);
    }
    
    // Eliminar una reseña (usando DTOs - método mejorado)
    public void deleteReview(String reviewId, String productId) {
        Review review = reviewRepository.findByIdAndProductId(reviewId, productId);
        
        if (review == null) {
            throw new RuntimeException("Reseña no encontrada");
        }
        
        reviewRepository.delete(review);
    }
}