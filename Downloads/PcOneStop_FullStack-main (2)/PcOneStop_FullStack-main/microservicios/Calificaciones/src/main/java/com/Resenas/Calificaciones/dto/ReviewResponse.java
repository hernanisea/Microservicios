package com.Resenas.Calificaciones.dto;

import java.io.Serializable;

public class ReviewResponse implements Serializable {
    
    private String id;
    private String productId;
    private String userId;
    private String author;
    private Integer rating;
    private String comment;
    private String date; // ISO string
    
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
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}

