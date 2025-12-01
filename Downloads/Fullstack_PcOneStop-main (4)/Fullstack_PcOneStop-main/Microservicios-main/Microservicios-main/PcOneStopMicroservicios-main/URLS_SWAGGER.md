# 🔗 URLs de Swagger UI - PcOneStop Microservicios

## 📋 URLs de Acceso a Swagger

### 1. **Microservicio Usuarios** (Autenticación)
- **Puerto:** `8081`
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **API Docs (JSON):** http://localhost:8081/v3/api-docs
- **Descripción:** Gestión de usuarios, registro e inicio de sesión

### 2. **Microservicio Inventario** (Productos)
- **Puerto:** `8082`
- **Swagger UI:** http://localhost:8082/swagger-ui.html
- **API Docs (JSON):** http://localhost:8082/v3/api-docs
- **Descripción:** Gestión de productos del catálogo

### 3. **Microservicio Pagos** (Pedidos)
- **Puerto:** `8083`
- **Swagger UI:** http://localhost:8083/swagger-ui.html
- **API Docs (JSON):** http://localhost:8083/v3/api-docs
- **Descripción:** Gestión de pedidos y órdenes

### 4. **Microservicio Calificaciones** (Reseñas)
- **Puerto:** `8084`
- **Swagger UI:** http://localhost:8084/swagger-ui.html
- **API Docs (JSON):** http://localhost:8084/v3/api-docs
- **Descripción:** Gestión de reseñas y calificaciones de productos

---

## 🔐 Cómo Usar Swagger con Autenticación JWT

### Paso 1: Obtener Token JWT

1. Abre Swagger del microservicio **Usuarios**: http://localhost:8081/swagger-ui.html
2. Busca el endpoint `/api/v1/auth/login` o `/api/v1/auth/register`
3. Haz clic en "Try it out"
4. Ingresa las credenciales:
   ```json
   {
     "email": "admin@test.com",
     "password": "password123"
   }
   ```
5. Ejecuta el request (Execute)
6. Copia el `token` de la respuesta

### Paso 2: Autenticarse en Swagger

1. En cualquier microservicio (Inventario, Pagos), busca el botón **"Authorize"** 🔓 (arriba a la derecha)
2. Haz clic en "Authorize"
3. En el campo "Value", pega el token JWT (sin "Bearer ")
4. Haz clic en "Authorize"
5. Cierra el diálogo
6. Ahora todos los endpoints protegidos estarán autenticados ✅

### Paso 3: Probar Endpoints Protegidos

- Los endpoints que requieren autenticación ahora mostrarán un candado 🔒
- Puedes probarlos directamente desde Swagger
- El token se enviará automáticamente en el header `Authorization: Bearer <token>`

---

## 📝 Endpoints Principales por Microservicio

### 🔵 Usuarios (Puerto 8081)

| Endpoint | Método | Descripción | Autenticación |
|----------|--------|-------------|---------------|
| `/api/v1/auth/register` | POST | Registrar nuevo usuario | ❌ Público |
| `/api/v1/auth/login` | POST | Iniciar sesión | ❌ Público |
| `/api/v1/auth` | GET | Listar todos los usuarios | ✅ Requerida |
| `/api/v1/auth/{id}` | GET | Obtener usuario por ID | ✅ Requerida |

**Swagger:** http://localhost:8081/swagger-ui.html

---

### 🟢 Inventario (Puerto 8082)

| Endpoint | Método | Descripción | Autenticación | Rol Requerido |
|----------|--------|-------------|---------------|---------------|
| `/api/v1/products` | GET | Listar todos los productos | ✅ | 🔴 ADMIN |
| `/api/v1/products/{id}` | GET | Obtener producto por ID | ❌ | Público |
| `/api/v1/products/offers` | GET | Productos en oferta | ❌ | Público |
| `/api/v1/products` | POST | Crear nuevo producto | ✅ | 🔴 ADMIN |
| `/api/v1/products/{id}` | PUT | Actualizar producto | ✅ | 🔴 ADMIN |
| `/api/v1/products/{id}` | DELETE | Eliminar producto | ✅ | 🔴 ADMIN |
| `/api/v1/products/{id}/stock` | PUT | Reducir stock | ✅ | Requerida |

**Swagger:** http://localhost:8082/swagger-ui.html

---

### 🟡 Pagos (Puerto 8083)

| Endpoint | Método | Descripción | Autenticación |
|----------|--------|-------------|---------------|
| `/api/v1/orders` | GET | Listar todos los pedidos | ✅ Requerida |
| `/api/v1/orders` | POST | Crear nuevo pedido | ✅ Requerida |
| `/api/v1/orders/{id}` | GET | Obtener pedido por ID | ✅ Requerida |
| `/api/v1/orders/user/{userId}` | GET | Pedidos de un usuario | ✅ Requerida |
| `/api/v1/orders/{id}/status` | PUT | Actualizar estado | ✅ Requerida |

**Swagger:** http://localhost:8083/swagger-ui.html

---

### 🟣 Calificaciones (Puerto 8084)

| Endpoint | Método | Descripción | Autenticación |
|----------|--------|-------------|---------------|
| `/api/v1/reviews` | GET | Listar todas las reseñas | ❌ Público |
| `/api/v1/reviews` | POST | Crear nueva reseña | ❌ Público |
| `/api/v1/reviews/product/{productId}` | GET | Reseñas de un producto | ❌ Público |
| `/api/v1/products/{productId}/reviews` | POST | Crear reseña para producto | ❌ Público |

**Swagger:** http://localhost:8084/swagger-ui.html

---

## 🎯 Ejemplo de Uso Completo

### Escenario: Crear un Producto (Requiere ADMIN)

1. **Obtener Token:**
   - Ve a http://localhost:8081/swagger-ui.html
   - Usa `/api/v1/auth/login` con credenciales de ADMIN
   - Copia el token de la respuesta

2. **Autenticarse en Inventario:**
   - Ve a http://localhost:8082/swagger-ui.html
   - Haz clic en "Authorize" 🔓
   - Pega el token
   - Haz clic en "Authorize"

3. **Crear Producto:**
   - Busca el endpoint `POST /api/v1/products`
   - Haz clic en "Try it out"
   - Ingresa los datos:
     ```json
     {
       "name": "GPU RTX 4070",
       "brand": "Nvidia",
       "model": "RTX 4070",
       "category": "GPU",
       "price": 700.0,
       "stock": 10,
       "description": "Tarjeta gráfica de alto rendimiento",
       "image": "https://example.com/image.jpg"
     }
     ```
   - Ejecuta el request
   - Verifica la respuesta exitosa (201 Created)

---

## 📸 Capturas de Pantalla Sugeridas para Documentación

Para tu evaluación, considera incluir capturas de:

1. **Swagger UI de cada microservicio** mostrando los endpoints
2. **Botón "Authorize"** visible y funcional
3. **Ejemplo de request autenticado** con token
4. **Respuesta exitosa** de un endpoint protegido
5. **Error 401/403** cuando no hay token o rol incorrecto

---

## 🔧 Configuración de Swagger

### Dependencia (pom.xml)
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

### Configuración (OpenApiConfig.java) - Solo en Inventario
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("PcOneStop - Microservicio Inventario API")
                .version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

---

## ⚠️ Notas Importantes

1. **Todos los microservicios deben estar ejecutándose** para acceder a Swagger
2. **El token JWT expira después de 24 horas** (configurable en `jwt.expiration`)
3. **El botón "Authorize" solo aparece** si hay configuración de seguridad en OpenAPI
4. **Los endpoints públicos** no requieren token (ej: GET /api/v1/products/{id})
5. **Los endpoints protegidos** muestran un candado 🔒 en Swagger

---

## 🚀 Acceso Rápido

Copia y pega estas URLs en tu navegador:

```
Usuarios:        http://localhost:8081/swagger-ui.html
Inventario:      http://localhost:8082/swagger-ui.html
Pagos:           http://localhost:8083/swagger-ui.html
Calificaciones:  http://localhost:8084/swagger-ui.html
```

---

## 📋 Checklist para Probar en Swagger

- [ ] Acceder a Swagger de Usuarios
- [ ] Hacer login y obtener token
- [ ] Acceder a Swagger de Inventario
- [ ] Hacer clic en "Authorize" y pegar token
- [ ] Probar endpoint público (GET /api/v1/products/{id})
- [ ] Probar endpoint protegido ADMIN (GET /api/v1/products)
- [ ] Verificar que funciona con token válido
- [ ] Verificar que rechaza sin token (401/403)

---

¡Listo para probar! 🎉


