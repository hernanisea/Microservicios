-- =====================================================
-- DATOS PRECARGADOS - MICROSERVICIO USUARIOS
-- Base de datos: db_usuarios
-- =====================================================

-- ==================== ROLES ====================
-- Insertamos los roles base del sistema
INSERT INTO roles (id, name, description) VALUES 
(1, 'ADMIN', 'Administrador del sistema con acceso total'),
(2, 'VENDEDOR', 'Usuario que puede publicar y gestionar productos'),
(3, 'CLIENTE', 'Usuario que puede comprar productos y dejar reseñas')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

-- ==================== USUARIOS DE PRUEBA ====================
-- Contraseña para todos: "password123" (ya encriptada con BCrypt)
-- Hash BCrypt de "password123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum

-- Admin del sistema
INSERT INTO users (id, first_name, last_name, email, password, phone, role_id) VALUES
(1, 'Carlos', 'Administrador', 'admin@pconestop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 999888777', 1)
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Vendedores
INSERT INTO users (id, first_name, last_name, email, password, phone, role_id) VALUES
(2, 'María', 'Tech Store', 'maria.tech@pconestop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 987654321', 2),
(3, 'José', 'PC Master', 'jose.pcmaster@pconestop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 912345678', 2),
(4, 'Ana', 'Gaming Pro', 'ana.gaming@pconestop.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 956789123', 2)
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Clientes
INSERT INTO users (id, first_name, last_name, email, password, phone, role_id) VALUES
(5, 'Pedro', 'García', 'pedro.garcia@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 945678901', 3),
(6, 'Lucía', 'Mendoza', 'lucia.mendoza@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 934567890', 3),
(7, 'Roberto', 'Sánchez', 'roberto.sanchez@hotmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 923456789', 3),
(8, 'Carmen', 'López', 'carmen.lopez@outlook.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 967890123', 3),
(9, 'Miguel', 'Torres', 'miguel.torres@yahoo.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 978901234', 3),
(10, 'Sofia', 'Ramírez', 'sofia.ramirez@gmail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGDJGxGVZaOeC/EiKOIEz9mTEJum', '+51 989012345', 3)
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- =====================================================
-- RESUMEN DE CREDENCIALES DE PRUEBA:
-- =====================================================
-- | Email                        | Contraseña    | Rol      |
-- |------------------------------|---------------|----------|
-- | admin@pconestop.com          | password123   | ADMIN    |
-- | maria.tech@pconestop.com     | password123   | VENDEDOR |
-- | jose.pcmaster@pconestop.com  | password123   | VENDEDOR |
-- | ana.gaming@pconestop.com     | password123   | VENDEDOR |
-- | pedro.garcia@gmail.com       | password123   | CLIENTE  |
-- | lucia.mendoza@gmail.com      | password123   | CLIENTE  |
-- | roberto.sanchez@hotmail.com  | password123   | CLIENTE  |
-- | carmen.lopez@outlook.com     | password123   | CLIENTE  |
-- | miguel.torres@yahoo.com      | password123   | CLIENTE  |
-- | sofia.ramirez@gmail.com      | password123   | CLIENTE  |
-- =====================================================

