package com.Catalogo.Inventario.service;

import com.Catalogo.Inventario.model.Category;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.CategoryRepository;
import com.Catalogo.Inventario.repository.ProductRepository;
import com.Catalogo.Inventario.repository.ReportRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ReportRepository reportRepository;

    /**
     * Obtiene todos los productos del catálogo.
     */
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    /**
     * Guarda un nuevo producto en el inventario.
     * Asigna la categoría correspondiente.
     */
    public Product save(Product product) {
        String categoryName = product.getCategory();
        if (categoryName == null || categoryName.isBlank()) {
            throw new RuntimeException("La categoría es obligatoria");
        }

        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + categoryName));

        product.setCategoryEntity(category);
        return productRepository.save(product);
    }

    /**
     * Busca un producto por su ID.
     */
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    /**
     * Descuenta stock de un producto.
     * Valida que haya suficiente inventario.
     */
    public Product reduceStock(Long id, Integer quantity) {
        Product product = findById(id);

        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente para el producto: " + product.getName() + 
                    ". Disponible: " + product.getStock() + ", Solicitado: " + quantity);
        }

        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    /**
     * Aumenta el stock de un producto.
     */
    public Product addStock(Long id, Integer quantity) {
        Product product = findById(id);
        product.setStock(product.getStock() + quantity);
        return productRepository.save(product);
    }

    /**
     * Actualiza la información de un producto.
     */
    public Product update(Long id, Product productDetails) {
        Product product = findById(id);
        
        product.setName(productDetails.getName());
        product.setBrand(productDetails.getBrand());
        product.setModel(productDetails.getModel());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setDescription(productDetails.getDescription());
        product.setImageUrl(productDetails.getImageUrl());

        if (productDetails.getCategory() != null) {
            Category category = categoryRepository.findByName(productDetails.getCategory())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + productDetails.getCategory()));
            product.setCategoryEntity(category);
        }

        return productRepository.save(product);
    }

    /**
     * Elimina un producto y sus reportes asociados.
     */
    public void deleteProduct(Long id) {
        reportRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    /**
     * Obtiene todas las categorías disponibles.
     */
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Busca productos por categoría.
     */
    public List<Product> findByCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + categoryName));
        return productRepository.findByCategoryEntity(category);
    }

    /**
     * Busca productos por vendedor.
     */
    public List<Product> findBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }
}

