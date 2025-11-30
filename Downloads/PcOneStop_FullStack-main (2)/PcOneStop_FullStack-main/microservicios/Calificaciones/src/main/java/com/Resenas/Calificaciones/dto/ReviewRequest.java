package com.Resenas.Calificaciones.dto;

import jakarta.validation.constraints.*;
import java.io.Serializable;

public class ReviewRequest implements Serializable {
    
    private String id; // Solo para actualizaciones
    
    @NotBlank(message = "El productId es requerido")
    private String productId;
    
    private String userId; // Puede ser null para reseñas anónimas
    
    private String author; // Opcional, se puede generar automáticamente
    
    @NotNull(message = "La calificación es requerida")
    @Min(value = 1, message = "La calificación debe ser al menos 1")
    @Max(value = 5, message = "La calificación debe ser máximo 5")
    private Integer rating;
    
    @NotBlank(message = "El comentario es requerido")
    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String comment;
    
    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}

