package com.Catalogo.Inventario.config;

import com.Catalogo.Inventario.model.Category;
import com.Catalogo.Inventario.model.Product;
import com.Catalogo.Inventario.repository.CategoryRepository;
import com.Catalogo.Inventario.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataLoader para precargar datos iniciales en la base de datos de Inventario.
 * Se ejecuta automáticamente al iniciar la aplicación.
 * 
 * Datos precargados:
 * - 6 Categorías de productos de computación
 * - 12+ Productos de ejemplo con imágenes
 * 
 * Los sellerId corresponden a:
 * - ID 2: María TechStore (vendedor1)
 * - ID 3: Pedro PCMaster (vendedor2)
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // Solo cargar si no hay datos
        if (categoryRepository.count() == 0) {
            loadCategories();
        }
        
        if (productRepository.count() == 0) {
            loadProducts();
        }
        
        System.out.println("[Inventario] Datos precargados correctamente");
        System.out.println("   Categorías: " + categoryRepository.count());
        System.out.println("   Productos: " + productRepository.count());
    }

    private void loadCategories() {
        System.out.println("Cargando categorías...");
        
        categoryRepository.save(new Category(null, "GPU", 
            "Tarjetas gráficas para gaming y trabajo profesional", 
            "https://cdn-icons-png.flaticon.com/512/2004/2004580.png"));
        
        categoryRepository.save(new Category(null, "CPU", 
            "Procesadores Intel y AMD para todo tipo de uso", 
            "https://cdn-icons-png.flaticon.com/512/2004/2004584.png"));
        
        categoryRepository.save(new Category(null, "RAM", 
            "Memorias RAM DDR4 y DDR5 de alta velocidad", 
            "https://cdn-icons-png.flaticon.com/512/2004/2004592.png"));
        
        categoryRepository.save(new Category(null, "SSD", 
            "Almacenamiento SSD NVMe y SATA ultra rápido", 
            "https://cdn-icons-png.flaticon.com/512/2004/2004577.png"));
        
        categoryRepository.save(new Category(null, "Motherboard", 
            "Placas madre compatibles con Intel y AMD", 
            "https://cdn-icons-png.flaticon.com/512/2004/2004571.png"));
        
        categoryRepository.save(new Category(null, "Periféricos", 
            "Teclados, mouse, audífonos y más", 
            "https://cdn-icons-png.flaticon.com/512/2004/2004597.png"));
        
        System.out.println("6 categorías creadas");
    }

    private void loadProducts() {
        System.out.println("Cargando productos de prueba...");
        
        Category gpu = categoryRepository.findByName("GPU").orElseThrow();
        Category cpu = categoryRepository.findByName("CPU").orElseThrow();
        Category ram = categoryRepository.findByName("RAM").orElseThrow();
        Category ssd = categoryRepository.findByName("SSD").orElseThrow();
        Category motherboard = categoryRepository.findByName("Motherboard").orElseThrow();
        Category perifericos = categoryRepository.findByName("Periféricos").orElseThrow();

        // GPUs
        Product rtx4070 = new Product();
        rtx4070.setName("GeForce RTX 4070 Super");
        rtx4070.setBrand("MSI");
        rtx4070.setModel("Ventus 3X OC");
        rtx4070.setCategoryEntity(gpu);
        rtx4070.setPrice(2599.00);
        rtx4070.setStock(8);
        rtx4070.setSellerId(2L);
        rtx4070.setDescription("Tarjeta gráfica NVIDIA RTX 4070 Super con 12GB GDDR6X, Ray Tracing y DLSS 3.0. Ideal para gaming 1440p.");
        rtx4070.setImageUrl("https://asset.msi.com/resize/image/global/product/product_1705040507dc5b22cab4dff0a31f9bfe8b5f9e9387.png62405b38c58fe0f07fcef2367d8a9ba1/1024.png");
        productRepository.save(rtx4070);

        Product rx7800xt = new Product();
        rx7800xt.setName("Radeon RX 7800 XT");
        rx7800xt.setBrand("ASUS");
        rx7800xt.setModel("TUF Gaming OC");
        rx7800xt.setCategoryEntity(gpu);
        rx7800xt.setPrice(2199.00);
        rx7800xt.setStock(5);
        rx7800xt.setSellerId(3L);
        rx7800xt.setDescription("GPU AMD Radeon con 16GB GDDR6. Excelente rendimiento en 1440p y compatible con FSR 3.");
        rx7800xt.setImageUrl("https://dlcdnwebimgs.asus.com/gain/87C0B7D1-B7A0-4B5D-9D18-C8E3A8E3D4F1/w1000/h732");
        productRepository.save(rx7800xt);

        Product rtx4060 = new Product();
        rtx4060.setName("GeForce RTX 4060 Ti");
        rtx4060.setBrand("Gigabyte");
        rtx4060.setModel("Gaming OC");
        rtx4060.setCategoryEntity(gpu);
        rtx4060.setPrice(1799.00);
        rtx4060.setStock(12);
        rtx4060.setSellerId(2L);
        rtx4060.setDescription("GPU RTX 4060 Ti 8GB, perfecta para gaming 1080p con Ray Tracing activado.");
        rtx4060.setImageUrl("https://static.gigabyte.com/StaticFile/Image/Global/c3e8c8d8c8d8c8d8c8d8c8d8c8d8c8d8/Product/31803/png/1000");
        productRepository.save(rtx4060);

        // CPUs
        Product ryzen7 = new Product();
        ryzen7.setName("Ryzen 7 7800X3D");
        ryzen7.setBrand("AMD");
        ryzen7.setModel("7800X3D");
        ryzen7.setCategoryEntity(cpu);
        ryzen7.setPrice(1899.00);
        ryzen7.setStock(10);
        ryzen7.setSellerId(2L);
        ryzen7.setDescription("El mejor procesador gaming de AMD. 8 núcleos, 16 hilos con tecnología 3D V-Cache de 96MB.");
        ryzen7.setImageUrl("https://m.media-amazon.com/images/I/51GoWBvYpWL._AC_SX679_.jpg");
        productRepository.save(ryzen7);

        Product i713700k = new Product();
        i713700k.setName("Intel Core i7-13700K");
        i713700k.setBrand("Intel");
        i713700k.setModel("13700K");
        i713700k.setCategoryEntity(cpu);
        i713700k.setPrice(1699.00);
        i713700k.setStock(7);
        i713700k.setSellerId(3L);
        i713700k.setDescription("Procesador Intel de 13ra gen. 16 núcleos (8P+8E), 24 hilos. Excelente para gaming y productividad.");
        i713700k.setImageUrl("https://m.media-amazon.com/images/I/51lO+E9gKbL._AC_SX679_.jpg");
        productRepository.save(i713700k);

        Product ryzen5 = new Product();
        ryzen5.setName("Ryzen 5 7600X");
        ryzen5.setBrand("AMD");
        ryzen5.setModel("7600X");
        ryzen5.setCategoryEntity(cpu);
        ryzen5.setPrice(999.00);
        ryzen5.setStock(15);
        ryzen5.setSellerId(2L);
        ryzen5.setDescription("Procesador gaming económico. 6 núcleos, 12 hilos. Arquitectura Zen 4 con soporte DDR5.");
        ryzen5.setImageUrl("https://m.media-amazon.com/images/I/51f2hkGHjpL._AC_SX679_.jpg");
        productRepository.save(ryzen5);

        // RAM
        Product ramCorsair = new Product();
        ramCorsair.setName("Vengeance DDR5 32GB");
        ramCorsair.setBrand("Corsair");
        ramCorsair.setModel("CMK32GX5M2B5600C36");
        ramCorsair.setCategoryEntity(ram);
        ramCorsair.setPrice(599.00);
        ramCorsair.setStock(20);
        ramCorsair.setSellerId(3L);
        ramCorsair.setDescription("Kit de 2x16GB DDR5-5600MHz CL36. RGB personalizable con iCUE. Ideal para builds gaming.");
        ramCorsair.setImageUrl("https://m.media-amazon.com/images/I/61RhvFuqDRL._AC_SX679_.jpg");
        productRepository.save(ramCorsair);

        Product ramGskill = new Product();
        ramGskill.setName("Trident Z5 RGB DDR5 32GB");
        ramGskill.setBrand("G.Skill");
        ramGskill.setModel("F5-6000J3038F16GX2-TZ5RK");
        ramGskill.setCategoryEntity(ram);
        ramGskill.setPrice(749.00);
        ramGskill.setStock(8);
        ramGskill.setSellerId(2L);
        ramGskill.setDescription("Kit premium 2x16GB DDR5-6000MHz CL30. El mejor rendimiento para overclock.");
        ramGskill.setImageUrl("https://m.media-amazon.com/images/I/61VKwqLx5ZL._AC_SX679_.jpg");
        productRepository.save(ramGskill);

        // SSDs
        Product ssd990Pro = new Product();
        ssd990Pro.setName("990 PRO 2TB");
        ssd990Pro.setBrand("Samsung");
        ssd990Pro.setModel("MZ-V9P2T0B/AM");
        ssd990Pro.setCategoryEntity(ssd);
        ssd990Pro.setPrice(799.00);
        ssd990Pro.setStock(15);
        ssd990Pro.setSellerId(3L);
        ssd990Pro.setDescription("SSD NVMe PCIe 4.0 x4 con velocidades de hasta 7,450 MB/s. Ideal para gaming y edición de video.");
        ssd990Pro.setImageUrl("https://m.media-amazon.com/images/I/71hTzf+DQFL._AC_SX679_.jpg");
        productRepository.save(ssd990Pro);

        Product ssdWD = new Product();
        ssdWD.setName("WD Black SN850X 1TB");
        ssdWD.setBrand("Western Digital");
        ssdWD.setModel("WDS100T2X0E");
        ssdWD.setCategoryEntity(ssd);
        ssdWD.setPrice(499.00);
        ssdWD.setStock(25);
        ssdWD.setSellerId(2L);
        ssdWD.setDescription("SSD gaming con hasta 7,300 MB/s. Optimizado para PS5 y PC. Disipador incluido.");
        ssdWD.setImageUrl("https://m.media-amazon.com/images/I/71gt0K2XWKL._AC_SX679_.jpg");
        productRepository.save(ssdWD);

        // Motherboards
        Product moboMSI = new Product();
        moboMSI.setName("MAG B650 TOMAHAWK WIFI");
        moboMSI.setBrand("MSI");
        moboMSI.setModel("MAG B650 TOMAHAWK");
        moboMSI.setCategoryEntity(motherboard);
        moboMSI.setPrice(899.00);
        moboMSI.setStock(6);
        moboMSI.setSellerId(2L);
        moboMSI.setDescription("Placa madre AM5 para Ryzen 7000. DDR5, PCIe 5.0, WiFi 6E, USB-C frontal.");
        moboMSI.setImageUrl("https://asset.msi.com/resize/image/global/product/product_1662689547ec2e9f9fdd0d4bcf5b7f4e32f7dc1cf9.png62405b38c58fe0f07fcef2367d8a9ba1/1024.png");
        productRepository.save(moboMSI);

        Product moboAsus = new Product();
        moboAsus.setName("ROG STRIX Z790-E Gaming");
        moboAsus.setBrand("ASUS");
        moboAsus.setModel("ROG STRIX Z790-E");
        moboAsus.setCategoryEntity(motherboard);
        moboAsus.setPrice(1899.00);
        moboAsus.setStock(3);
        moboAsus.setSellerId(3L);
        moboAsus.setDescription("Motherboard premium Intel LGA1700. DDR5, PCIe 5.0 x16, WiFi 6E, Thunderbolt 4.");
        moboAsus.setImageUrl("https://dlcdnwebimgs.asus.com/gain/7ac6b4f4-9e68-4a9c-b5d1-9c8f8e8c8f8c/w1000/h732");
        productRepository.save(moboAsus);

        // Periféricos
        Product teclado = new Product();
        teclado.setName("G Pro X TKL");
        teclado.setBrand("Logitech");
        teclado.setModel("920-009239");
        teclado.setCategoryEntity(perifericos);
        teclado.setPrice(449.00);
        teclado.setStock(18);
        teclado.setSellerId(2L);
        teclado.setDescription("Teclado mecánico gaming TKL con switches intercambiables. RGB LIGHTSYNC.");
        teclado.setImageUrl("https://m.media-amazon.com/images/I/71fb48twi-L._AC_SX679_.jpg");
        productRepository.save(teclado);

        Product mouse = new Product();
        mouse.setName("DeathAdder V3 Pro");
        mouse.setBrand("Razer");
        mouse.setModel("RZ01-04630100");
        mouse.setCategoryEntity(perifericos);
        mouse.setPrice(649.00);
        mouse.setStock(12);
        mouse.setSellerId(3L);
        mouse.setDescription("Mouse gaming inalámbrico ultraligero (63g). Sensor Focus Pro 30K, 90 horas de batería.");
        mouse.setImageUrl("https://m.media-amazon.com/images/I/61mpMH5TzkL._AC_SX679_.jpg");
        productRepository.save(mouse);

        System.out.println("14 productos creados en 6 categorías");
        System.out.println("   GPU: RTX 4070 Super, RX 7800 XT, RTX 4060 Ti");
        System.out.println("   CPU: Ryzen 7 7800X3D, i7-13700K, Ryzen 5 7600X");
        System.out.println("   RAM: Corsair DDR5, G.Skill DDR5");
        System.out.println("   SSD: Samsung 990 PRO, WD Black SN850X");
        System.out.println("   Motherboard: MSI B650, ASUS Z790");
        System.out.println("   Periféricos: Logitech G Pro X, Razer DeathAdder");
    }
}




