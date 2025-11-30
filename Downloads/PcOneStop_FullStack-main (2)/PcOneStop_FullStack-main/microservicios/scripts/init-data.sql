-- ============================================
-- SCRIPT DE INICIALIZACIÓN DE DATOS
-- PcOneStop Microservicios
-- ============================================

-- Crear las bases de datos si no existen
CREATE DATABASE IF NOT EXISTS db_usuarios;
CREATE DATABASE IF NOT EXISTS db_inventario;
CREATE DATABASE IF NOT EXISTS db_pedidos;
CREATE DATABASE IF NOT EXISTS db_calificaciones;

-- ============================================
-- DATOS DE USUARIOS (db_usuarios)
-- ============================================
USE db_usuarios;

-- Limpiar tabla existente
DELETE FROM users WHERE id IS NOT NULL;

-- Insertar usuarios de prueba
-- Nota: Las contraseñas están en texto plano aquí, pero el microservicio las encriptará
-- Para login usa: admin@pconestop.com / admin  |  cliente@gmail.com / 123

INSERT INTO users (id, name, last_name, email, password, role) VALUES
('user-admin-01', 'Hernán', 'Admin', 'admin@pconestop.com', '$2a$10$N.NiKQ5wRhVqj1Y5JQnQs.yC5FWXU5Qfh5nHhRdJ5GqXdDfnHqPVm', 'ADMIN'),
('user-client-01', 'Pedro', 'Pérez', 'cliente@gmail.com', '$2a$10$kZx8bWqDKQ.H5E3hB5GfMe8RqGWqjKxWKzXzOqHjKl0H5JKqZOPVm', 'CLIENT');

-- ============================================
-- DATOS DE PRODUCTOS (db_inventario)
-- ============================================
USE db_inventario;

-- Limpiar tablas existentes
DELETE FROM product_reports WHERE id IS NOT NULL;
DELETE FROM products WHERE id IS NOT NULL;

-- Insertar productos de prueba
INSERT INTO products (id, name, category, brand, price, stock, image, description, is_on_sale, offer_discount, offer_start_date, offer_end_date) VALUES
('cpu-ryzen-5600', 'AMD Ryzen 5 5600', 'CPU', 'AMD', 129990, 20, 
 'https://media.spdigital.cl/thumbnails/products/of2nke17_20c552f2_thumbnail_4096.png',
 '6C/12T, gran rendimiento precio-rendimiento.', 
 TRUE, 15, '2025-01-01', '2025-12-31'),

('gpu-rtx-4060', 'NVIDIA GeForce RTX 4060', 'GPU', 'NVIDIA', 349990, 10,
 'https://images-na.ssl-images-amazon.com/images/I/71U826jfF1L.jpg',
 'Ada Lovelace, DLSS 3, ideal 1080p/1440p.',
 FALSE, NULL, NULL, NULL),

('ram-ddr5-16', 'DDR5 16GB 6000MHz', 'RAM', 'Corsair', 69990, 50,
 'https://centrale.cl/wp-content/uploads/Memoria-RAM-32GB-2x-16GB-DDR5-6000MT-s-CL30-Kingston-Fury-Beast-RGB_CWaRYf8.webp',
 'Kit 2x8GB, CL36.',
 FALSE, NULL, NULL, NULL),

('mb-b550-asus', 'ASUS TUF Gaming B550-PLUS', 'Placa madre', 'ASUS', 159990, 15,
 'https://dlcdnweb.asus.com/product/a4a83355-7d04-46c5-9b1a-20f59a16f2c3/P_setting_000_1_90_end_s.png',
 'Placa madre ATX, AM4, ideal para Ryzen 5000.',
 TRUE, 10, '2025-01-01', '2025-12-31'),

('psu-corsair-750w', 'Corsair RM750x 750W 80+ Gold', 'Fuente', 'Corsair', 109990, 25,
 'https://www.corsair.com/medias/sys_master/images/images/h8f/h30/8883584598046/CP-9020179-NA/Gallery/RMx_2018_750_01_Front.png',
 'Fuente de poder modular, 750W, certificación 80+ Gold.',
 TRUE, 12, '2025-01-01', '2025-12-31'),

('ssd-kingston-nv2-1tb', 'Kingston NV2 1TB NVMe PCIe 4.0', 'Almacenamiento', 'Kingston', 59990, 40,
 'https://media.kingston.com/kingston/product/ktc-product-ssd-nv2-nvme-pcie-snv2s-1000g-1-sm.png',
 'SSD NVMe M.2 1TB. Velocidades de lectura/escritura de hasta 3500/2100 MB/s.',
 FALSE, NULL, NULL, NULL),

('case-nzxt-h5-flow', 'Gabinete NZXT H5 Flow', 'Gabinete', 'NZXT', 84990, 18,
 'https://nzxt.com/assets/cms/34299/1665005825-h5flow-white-main-no-system.png?auto=format&fit=fill&h=1000&w=1000',
 'Gabinete media torre con panel frontal perforado para alto flujo de aire. Incluye 2 ventiladores.',
 FALSE, NULL, NULL, NULL),

('cooler-deepcool-ag400', 'Cooler CPU Deepcool AG400', 'Cooler', 'Deepcool', 24990, 30,
 'https://www.deepcool.com/product/CPUPACKAGE/2022-07/19_19502/web/AG400-BK-ARDB-1.png',
 'Disipador de aire para CPU con 4 tubos de calor de contacto directo y ventilador de 120mm.',
 FALSE, NULL, NULL, NULL);

-- ============================================
-- DATOS DE PEDIDOS (db_pedidos)
-- ============================================
USE db_pedidos;

-- Limpiar tablas existentes
DELETE FROM order_items WHERE order_id IS NOT NULL;
DELETE FROM orders WHERE id IS NOT NULL;

-- Insertar pedido de ejemplo
INSERT INTO orders (id, total, created_at, customer_email, customer_name, customer_last_name, 
                   shipping_street, shipping_department, shipping_region, shipping_comuna, shipping_indications) VALUES
('20240705001', 129990, '2025-01-15T10:30:00', 'cliente@gmail.com', 'Pedro', 'Pérez',
 'Calle Falsa 123', 'Depto 603', 'Región Metropolitana', 'Cerrillos', 'Dejar en conserjería');

-- Insertar items del pedido
INSERT INTO order_items (order_id, product_id, name, price, qty) VALUES
('20240705001', 'cpu-ryzen-5600', 'AMD Ryzen 5 5600', 129990, 1);

-- ============================================
-- FIN DEL SCRIPT
-- ============================================
SELECT 'Datos inicializados correctamente!' AS mensaje;

