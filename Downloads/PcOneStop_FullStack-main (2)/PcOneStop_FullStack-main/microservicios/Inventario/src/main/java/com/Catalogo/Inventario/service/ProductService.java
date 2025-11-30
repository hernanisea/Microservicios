package com.Catalogo.Inventario.service;

import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.ProductRepository;
import com.Catalogo.Inventario.repository.ReportRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReportRepository reportRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product save(Product product) {
        // Generar ID si no viene (para nuevos productos)
        if (product.getId() == null || product.getId().isEmpty()) {
            product.setId(generateProductId(product.getName(), product.getCategory()));
        }
        // Asegurar que isOnSale tenga valor por defecto
        if (product.getIsOnSale() == null) {
            product.setIsOnSale(false);
        }
        return productRepository.save(product);
    }

    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> findOnSale() {
        return productRepository.findByIsOnSaleTrue();
    }

    public Product update(String id, Product productData) {
        Product existingProduct = findById(id);
        
        existingProduct.setName(productData.getName());
        existingProduct.setCategory(productData.getCategory());
        existingProduct.setBrand(productData.getBrand());
        existingProduct.setPrice(productData.getPrice());
        existingProduct.setStock(productData.getStock());
        existingProduct.setImage(productData.getImage());
        existingProduct.setDescription(productData.getDescription());
        existingProduct.setIsOnSale(productData.getIsOnSale());
        existingProduct.setOffer(productData.getOffer());
        
        return productRepository.save(existingProduct);
    }

    public Product reduceStock(String id, Integer quantity) {
        Product product = findById(id);

        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente para el producto: " + product.getName());
        }

        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        // Primero borramos los reportes asociados
        reportRepository.deleteByProductId(id);
        // Ahora borramos el producto
        productRepository.deleteById(id);
    }

    // Genera un ID tipo slug basado en nombre y categoría
    private String generateProductId(String name, String category) {
        String slug = (category + "-" + name)
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        
        // Agregar sufijo único si el slug ya existe
        if (productRepository.existsById(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 6);
        }
        return slug;
    }
}
