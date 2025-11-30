package com.Resenas.Calificaciones.repository;

import com.Resenas.Calificaciones.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    // Obtener reseñas de un producto específico ordenadas por fecha descendente
    List<Review> findByProductIdOrderByDateDesc(String productId);
    
    // Obtener reseñas de un producto específico (método original para compatibilidad)
    List<Review> findByProductId(String productId);
    
    // Buscar reseña por ID y productId
    Review findByIdAndProductId(String id, String productId);
    
    // Buscar reseña existente de un usuario para un producto
    Optional<Review> findByUserIdAndProductId(String userId, String productId);
    
    // Buscar reseña de un usuario para un producto (alternativa)
    Review findByProductIdAndUserId(String productId, String userId);
    
    // Obtener reseñas de un usuario
    List<Review> findByUserId(String userId);
    
    // Contar reseñas de un producto
    long countByProductId(String productId);
}
