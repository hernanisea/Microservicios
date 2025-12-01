package com.Catalogo.Inventario.service;

import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.ProductRepository;
import com.Catalogo.Inventario.repository.ReportRepository; // Importar esto
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
    private ReportRepository reportRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
    }

    // Lógica para descontar stock
    public Product reduceStock(Long id, Integer quantity) {
        Product product = findById(id);

        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente para el producto: " + product.getName());
        }

        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        // 1. Primero borramos los reportes asociados (para evitar error de Foreign Key)
        reportRepository.deleteByProductId(id);
        
        // 2. Ahora sí, borramos el producto
        productRepository.deleteById(id);
    }

    public List<Product> findOnSaleProducts() {
        return productRepository.findByIsOnSaleTrue();
    }
}