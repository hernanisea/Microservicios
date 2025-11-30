# 🖥️ PcOneStop - Microservicios

Sistema de microservicios para tienda de componentes de PC, compatible con el frontend React de referencia.

## 📋 Arquitectura

| Microservicio | Puerto | Base de Datos | Descripción |
|--------------|--------|---------------|-------------|
| **Usuarios** | 8081 | db_usuarios | Autenticación y gestión de usuarios |
| **Inventario** | 8082 | db_inventario | Catálogo de productos y stock |
| **Pagos** | 8083 | db_pedidos | Gestión de órdenes de compra |
| **Calificaciones** | 8084 | db_calificaciones | Reseñas y valoraciones |

## 🚀 Requisitos

- **Java 21+**
- **MySQL 8+** (Laragon, XAMPP, o MySQL Server)
- **Maven 3.8+**
- **Node.js 18+** (para el frontend)

## ⚡ Inicio Rápido

### 1. Iniciar MySQL

Asegúrate de que MySQL esté corriendo en `localhost:3306` con usuario `root` sin contraseña.

### 2. Ejecutar cada microservicio

Abre 4 terminales, una para cada microservicio:

```bash
# Terminal 1 - Usuarios
cd Usuarios
./mvnw spring-boot:run

# Terminal 2 - Inventario
cd Inventario
./mvnw spring-boot:run

# Terminal 3 - Pagos
cd Pagos
./mvnw spring-boot:run

# Terminal 4 - Calificaciones
cd Calificaciones
./mvnw spring-boot:run
```

En Windows, usa `mvnw.cmd` en lugar de `./mvnw`.

### 3. Verificar que los servicios estén corriendo

- Usuarios: http://localhost:8081/swagger-ui.html
- Inventario: http://localhost:8082/swagger-ui.html
- Pagos: http://localhost:8083/swagger-ui.html
- Calificaciones: http://localhost:8084/swagger-ui.html

### 4. (Opcional) Cargar datos de prueba

```bash
mysql -u root < scripts/init-data.sql
```

## 📡 Endpoints Principales

### Usuarios (8081)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Registrar usuario |
| POST | `/api/v1/auth/login` | Iniciar sesión |
| GET | `/api/v1/auth` | Listar usuarios |
| GET | `/api/v1/auth/{id}` | Obtener usuario |
| PUT | `/api/v1/auth/{id}` | Actualizar usuario |
| DELETE | `/api/v1/auth/{id}` | Eliminar usuario |

### Inventario (8082)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/products` | Listar productos |
| GET | `/api/v1/products/{id}` | Obtener producto |
| GET | `/api/v1/products/category/{cat}` | Por categoría |
| GET | `/api/v1/products/offers` | En oferta |
| POST | `/api/v1/products` | Crear producto |
| PUT | `/api/v1/products/{id}` | Actualizar producto |
| DELETE | `/api/v1/products/{id}` | Eliminar producto |

### Pagos (8083)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/orders` | Listar pedidos |
| GET | `/api/v1/orders/{id}` | Obtener pedido |
| GET | `/api/v1/orders/customer/{email}` | Por cliente |
| POST | `/api/v1/orders` | Crear pedido |
| DELETE | `/api/v1/orders/{id}` | Eliminar pedido |

### Calificaciones (8084)
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/reviews` | Listar reseñas |
| GET | `/api/v1/reviews/product/{id}` | Por producto |
| POST | `/api/v1/reviews` | Crear reseña |
| DELETE | `/api/v1/reviews/{id}` | Eliminar reseña |

## 🔧 Configuración

### Cambiar credenciales de BD

Edita `application.properties` de cada microservicio:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_nombre
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

### Primera ejecución

Los archivos `application.properties` tienen `ddl-auto=create` para crear las tablas automáticamente. **Después de la primera ejecución, cámbialo a `update`** para no perder datos:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## 🎨 Frontend

El frontend está en la carpeta `referencia_proyecto/Fullstack_PcOneStop-main/`:

```bash
cd referencia_proyecto/Fullstack_PcOneStop-main
npm install
npm run dev
```

Abrirá en http://localhost:5173

## 📝 Usuarios de Prueba

| Email | Contraseña | Rol |
|-------|------------|-----|
| admin@pconestop.com | admin | ADMIN |
| cliente@gmail.com | 123 | CLIENT |

> ⚠️ Nota: Para usar estos usuarios, primero debes registrarlos a través del endpoint `/api/v1/auth/register` o ejecutar el script SQL de inicialización.

## 🐛 Solución de Problemas

### Error de conexión a MySQL
- Verifica que MySQL esté corriendo
- Confirma usuario `root` sin contraseña
- Crea las BD manualmente si es necesario:
  ```sql
  CREATE DATABASE db_usuarios;
  CREATE DATABASE db_inventario;
  CREATE DATABASE db_pedidos;
  CREATE DATABASE db_calificaciones;
  ```

### Error CORS
Los microservicios están configurados para aceptar requests de:
- http://localhost:5173
- http://localhost:3000

### Puerto ocupado
Verifica que los puertos 8081-8084 estén libres o cambia en `application.properties`:
```properties
server.port=NUEVO_PUERTO
```

## 📄 Licencia

MIT © 2025

