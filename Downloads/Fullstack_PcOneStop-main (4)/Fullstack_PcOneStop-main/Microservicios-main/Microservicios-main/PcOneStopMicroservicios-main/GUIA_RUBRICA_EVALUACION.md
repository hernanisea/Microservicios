# 📋 Guía para Responder la Rúbrica de Evaluación

## 🎯 Estructura General

Cada respuesta debe incluir:
1. **Evidencia de implementación** (código, archivos, capturas)
2. **Explicación técnica** (cómo funciona, por qué se hizo así)
3. **Justificación** (decisiones de diseño, mejores prácticas)

---

## 📌 IE3.1.1: Crea aplicación backend con BD, lógica de negocio y modelos (8%)

### ✅ Qué Debes Demostrar:
- Backend creado con Spring Boot
- Conexión a base de datos (MySQL)
- Modelos de datos (entidades JPA)
- Lógica de negocio (servicios)
- Repositorios (JPA Repository)

### 📝 Estructura de Respuesta:

```
1. INTRODUCCIÓN
   - Se desarrolló una aplicación backend con arquitectura de microservicios
   - 4 microservicios: Usuarios, Inventario, Pagos, Calificaciones
   - Framework: Spring Boot 3.5.7/3.5.8
   - Base de datos: MySQL

2. CONEXIÓN A BASE DE DATOS
   - Configuración en application.properties
   - Driver: mysql-connector-j
   - JPA/Hibernate para ORM

3. MODELOS DE DATOS
   - Entidades JPA con anotaciones @Entity, @Table, @Column
   - Relaciones entre entidades
   - Validaciones con Bean Validation

4. LÓGICA DE NEGOCIO
   - Servicios con @Service
   - Transacciones con @Transactional
   - Validaciones de negocio

5. EVIDENCIAS
   - Capturas de código
   - Estructura de carpetas
   - Diagrama de entidades (opcional)
```

### 🔍 Evidencias a Incluir:

**1. Configuración de Base de Datos:**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/pconestop_db
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**2. Modelo de Datos (Ejemplo: Product):**
```java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String brand;
    
    // ... más campos
}
```

**3. Lógica de Negocio (Ejemplo: ProductService):**
```java
@Service
@Transactional
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    public Product reduceStock(Long id, Integer quantity) {
        Product product = findById(id);
        if (product.getStock() < quantity) {
            throw new RuntimeException("Stock insuficiente");
        }
        product.setStock(product.getStock() - quantity);
        return productRepository.save(product);
    }
}
```

### 📊 Puntos Clave a Mencionar:
- ✅ Arquitectura de microservicios
- ✅ Separación de responsabilidades (Controller → Service → Repository)
- ✅ Uso de JPA/Hibernate para ORM
- ✅ Validaciones de negocio en servicios
- ✅ Transacciones para consistencia de datos

---

## 📌 IE3.1.2: Describe el desarrollo de aplicación backend con BD (12%)

### ✅ Qué Debes Explicar:
- **Proceso de desarrollo** paso a paso
- **Decisiones de diseño** (por qué microservicios, por qué MySQL)
- **Arquitectura** elegida
- **Patrones** utilizados

### 📝 Estructura de Respuesta:

```
1. ARQUITECTURA ELEGIDA
   - Arquitectura de microservicios
   - Justificación: Separación de responsabilidades, escalabilidad
   - Cada microservicio tiene su propia base de datos (opcional)

2. PROCESO DE DESARROLLO
   a) Análisis de requerimientos
   b) Diseño de modelos de datos
   c) Configuración de base de datos
   d) Implementación de entidades JPA
   e) Desarrollo de servicios de negocio
   f) Implementación de repositorios

3. DECISIONES TÉCNICAS
   - Spring Boot: Framework robusto, ecosistema maduro
   - MySQL: Base de datos relacional confiable
   - JPA/Hibernate: ORM para simplificar acceso a datos
   - Lombok: Reducción de código boilerplate

4. MODELOS DE DATOS
   - Descripción de cada entidad
   - Relaciones entre entidades
   - Validaciones implementadas

5. LÓGICA DE NEGOCIO
   - Reglas de negocio implementadas
   - Validaciones en servicios
   - Manejo de transacciones
```

### 🔍 Ejemplo de Respuesta:

```
El desarrollo de la aplicación backend se realizó siguiendo una arquitectura 
de microservicios, lo que permite separar las responsabilidades en módulos 
independientes: Usuarios, Inventario, Pagos y Calificaciones.

Para la conexión a la base de datos, se configuró MySQL utilizando Spring 
Data JPA y Hibernate como ORM. Esto permite mapear las entidades Java a 
tablas de la base de datos de forma automática, reduciendo la complejidad 
del código SQL.

Los modelos de datos se implementaron como entidades JPA con anotaciones 
como @Entity, @Table y @Column. Por ejemplo, la entidad Product representa 
un producto del catálogo con campos como name, brand, price, stock, etc.

La lógica de negocio se implementó en servicios (@Service) que encapsulan 
las reglas del dominio. Por ejemplo, el método reduceStock() valida que 
haya suficiente stock antes de descontar unidades, lanzando una excepción 
si no se cumple la condición.

Se utilizó Lombok para reducir código boilerplate, generando automáticamente 
getters, setters y constructores mediante anotaciones como @Data.
```

---

## 📌 IE3.2.1: Implementa API REST con Spring Boot y Swagger (8%)

### ✅ Qué Debes Demostrar:
- Endpoints REST (GET, POST, PUT, DELETE)
- Operaciones CRUD completas
- Documentación en Swagger
- Respuestas estructuradas

### 📝 Estructura de Respuesta:

```
1. ENDPOINTS IMPLEMENTADOS
   - GET: Obtener recursos
   - POST: Crear recursos
   - PUT: Actualizar recursos
   - DELETE: Eliminar recursos

2. DOCUMENTACIÓN SWAGGER
   - Configuración de OpenAPI
   - Anotaciones en controladores
   - Ejemplos de requests/responses

3. ESTRUCTURA DE RESPUESTAS
   - Formato estándar (ApiResponse)
   - Códigos de estado HTTP
   - Mensajes descriptivos

4. EVIDENCIAS
   - Capturas de Swagger UI
   - Código de controladores
   - Ejemplos de requests/responses
```

### 🔍 Evidencias a Incluir:

**1. Controlador REST (Ejemplo):**
```java
@RestController
@RequestMapping("/api/v1/products")
@Tag(name = "Productos", description = "Gestión de productos")
public class ProductController {
    
    @GetMapping
    @Operation(summary = "Listar todos los productos")
    public ResponseEntity<ApiResponse<List<Product>>> list() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
            true, 200, "Lista de productos", products, (long) products.size()
        ));
    }
    
    @PostMapping
    @Operation(summary = "Crear nuevo producto")
    public ResponseEntity<ApiResponse<Product>> save(...) {
        // ...
    }
}
```

**2. Configuración Swagger:**
```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
            .info(new Info()
                .title("PcOneStop API")
                .version("1.0.0"));
    }
}
```

**3. Capturas de Swagger UI:**
- Pantalla completa de Swagger UI
- Endpoints listados
- Botón "Authorize" visible
- Ejemplo de request/response

### 📊 Puntos Clave a Mencionar:
- ✅ CRUD completo implementado
- ✅ Documentación automática con Swagger
- ✅ Respuestas estructuradas (ApiResponse)
- ✅ Códigos HTTP apropiados (200, 201, 400, 404, 500)
- ✅ Validaciones en endpoints

---

## 📌 IE3.2.3: Explica implementación de API REST con Spring Boot (12%)

### ✅ Qué Debes Explicar:
- **Cómo** se implementaron los endpoints
- **Por qué** se usó Spring Boot
- **Cómo** funciona Swagger
- **Decisiones** de diseño

### 📝 Estructura de Respuesta:

```
1. FRAMEWORK ELEGIDO: SPRING BOOT
   - Justificación: Ecosistema maduro, facilita desarrollo REST
   - Ventajas: Configuración automática, servidor embebido

2. IMPLEMENTACIÓN DE ENDPOINTS
   - Uso de @RestController
   - Mapeo de rutas con @RequestMapping
   - Métodos HTTP con @GetMapping, @PostMapping, etc.
   - Manejo de parámetros (@PathVariable, @RequestParam, @RequestBody)

3. DOCUMENTACIÓN CON SWAGGER
   - Integración de springdoc-openapi
   - Anotaciones @Operation, @ApiResponse, @Tag
   - Configuración de seguridad JWT en Swagger

4. ESTRUCTURA DE RESPUESTAS
   - Clase ApiResponse para formato estándar
   - Códigos de estado HTTP semánticos
   - Mensajes descriptivos

5. VALIDACIONES
   - Bean Validation (@NotNull, @NotBlank, @Email)
   - Validaciones personalizadas en servicios
   - Manejo de excepciones con GlobalExceptionHandler
```

### 🔍 Ejemplo de Respuesta:

```
La implementación de la API REST se realizó utilizando Spring Boot, framework 
que simplifica el desarrollo de aplicaciones Java mediante configuración 
automática y un servidor embebido (Tomcat).

Los endpoints se implementaron en controladores REST utilizando la anotación 
@RestController, que combina @Controller y @ResponseBody. Cada controlador 
mapea una ruta base con @RequestMapping, por ejemplo /api/v1/products.

Las operaciones CRUD se implementaron con métodos anotados:
- @GetMapping para operaciones de lectura
- @PostMapping para creación de recursos
- @PutMapping para actualización
- @DeleteMapping para eliminación

Para la documentación, se integró Swagger mediante springdoc-openapi, que 
genera automáticamente la documentación OpenAPI 3.0 a partir de anotaciones 
en los controladores. Se configuró el esquema de seguridad JWT para que los 
usuarios puedan autenticarse directamente desde Swagger UI.

Las respuestas se estructuraron en un formato estándar mediante la clase 
ApiResponse, que incluye campos como ok (boolean), statusCode (int), message 
(String), data (Object) y count (Long). Esto facilita el manejo de respuestas 
en el frontend.
```

---

## 📌 IE3.2.2: Implementa integración backend-frontend (6%)

### ✅ Qué Debes Demostrar:
- CORS configurado
- Comunicación exitosa entre frontend y backend
- Manejo de respuestas JSON
- Headers apropiados

### 📝 Estructura de Respuesta:

```
1. CONFIGURACIÓN CORS
   - Orígenes permitidos
   - Métodos HTTP permitidos
   - Headers permitidos

2. COMUNICACIÓN API REST
   - Ejemplos de requests desde frontend
   - Ejemplos de responses del backend
   - Formato JSON

3. EVIDENCIAS
   - Código de configuración CORS
   - Ejemplos de requests/responses
   - Capturas de Network (opcional)
```

### 🔍 Evidencias a Incluir:

**1. Configuración CORS:**
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:5173",
        "http://localhost:3000"
    ));
    configuration.setAllowedMethods(Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS"
    ));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);
    return source;
}
```

**2. Ejemplo de Request Frontend:**
```javascript
const response = await fetch('http://localhost:8082/api/v1/products', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## 📌 IE3.2.4: Justifica integración backend-frontend (10%)

### ✅ Qué Debes Justificar:
- **Por qué** se configuró CORS de esa manera
- **Cómo** se asegura la comunicación eficiente
- **Decisiones** de diseño
- **Flujo** de datos

### 📝 Estructura de Respuesta:

```
1. NECESIDAD DE CORS
   - Frontend y backend en diferentes orígenes
   - Política de mismo origen del navegador
   - Solución: Configuración CORS en backend

2. CONFIGURACIÓN IMPLEMENTADA
   - Orígenes permitidos: localhost:5173, localhost:3000
   - Métodos HTTP: GET, POST, PUT, DELETE, OPTIONS
   - Headers: Authorization (para JWT), Content-Type
   - Credenciales: true (permite cookies y headers de autorización)

3. FLUJO DE COMUNICACIÓN
   - Frontend envía request con token JWT
   - Backend valida y procesa
   - Backend retorna respuesta JSON estructurada
   - Frontend procesa y actualiza UI

4. FORMATO DE DATOS
   - JSON como formato estándar
   - Estructura ApiResponse para consistencia
   - Códigos HTTP semánticos

5. VENTAJAS DE LA IMPLEMENTACIÓN
   - Separación de frontend y backend
   - Escalabilidad
   - Reutilización de API
```

---

## 📌 IE3.3.1: Implementa autenticación con roles y JWT (6%)

### ✅ Qué Debes Demostrar:
- Generación de tokens JWT
- Validación de tokens
- Roles implementados (ADMIN, CLIENTE)
- Restricciones de acceso por rol

### 📝 Estructura de Respuesta:

```
1. GENERACIÓN DE TOKENS
   - JwtUtil.generateToken()
   - Token incluye: email, role, userId
   - Firma criptográfica

2. VALIDACIÓN DE TOKENS
   - JwtAuthenticationFilter
   - Validación en cada request
   - Extracción de rol

3. AUTORIZACIÓN POR ROLES
   - SecurityConfig con hasRole()
   - Endpoints protegidos por rol
   - Ejemplos: Solo ADMIN puede crear productos

4. EVIDENCIAS
   - Código de JwtUtil
   - Código de JwtAuthenticationFilter
   - Código de SecurityConfig
   - Capturas de Swagger con botón Authorize
```

### 🔍 Evidencias a Incluir:

**1. Generación de Token:**
```java
public String generateToken(String email, String role, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);
    claims.put("userId", userId);
    return createToken(claims, email);
}
```

**2. Validación en Filtro:**
```java
if (jwtUtil.validateToken(token)) {
    String role = jwtUtil.extractRole(token);
    String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
    // Establecer autenticación
}
```

**3. Restricción por Rol:**
```java
.requestMatchers("POST", "/api/v1/products").hasRole("ADMIN")
```

---

## 📌 IE3.3.4: Describe autenticación con roles y JWT (10%)

### ✅ Qué Debes Explicar:
- **Cómo** funciona JWT
- **Cómo** se implementaron los roles
- **Cómo** se valida el token
- **Flujo** completo de autenticación

### 📝 Estructura de Respuesta:

```
1. AUTENTICACIÓN BASADA EN TOKENS (JWT)
   - Qué es JWT y por qué se eligió
   - Estructura del token (Header.Payload.Signature)
   - Información incluida en el token (email, role, userId)

2. GENERACIÓN DEL TOKEN
   - Proceso al hacer login/registro
   - Inclusión del rol en el token
   - Firma criptográfica con clave secreta
   - Expiración (24 horas)

3. VALIDACIÓN DEL TOKEN
   - JwtAuthenticationFilter intercepta cada request
   - Extracción del token del header Authorization
   - Validación de firma y expiración
   - Extracción de información (email, role, userId)

4. AUTORIZACIÓN POR ROLES
   - Roles implementados: ADMIN, CLIENTE
   - SecurityConfig define permisos por endpoint
   - hasRole("ADMIN") para recursos administrativos
   - authenticated() para recursos que requieren login

5. FLUJO COMPLETO
   - Usuario hace login → Backend genera token con rol
   - Cliente guarda token → Envía en cada request
   - Backend valida token → Verifica rol → Permite/rechaza acceso

6. SEGURIDAD
   - Token firmado criptográficamente
   - Expiración automática
   - Validación en cada request
   - Clave secreta compartida entre microservicios
```

### 🔍 Ejemplo de Respuesta:

```
La autenticación se implementó utilizando JWT (JSON Web Tokens), un estándar 
que permite transmitir información de forma segura entre frontend y backend.

Al hacer login o registro, el backend genera un token JWT que incluye:
- Email del usuario (subject)
- Rol (ADMIN o CLIENTE)
- ID del usuario
- Fecha de emisión y expiración (24 horas)

El token se firma criptográficamente con una clave secreta usando HMAC SHA-256, 
lo que garantiza que no pueda ser modificado sin ser detectado.

En cada request HTTP, el JwtAuthenticationFilter intercepta la petición, 
extrae el token del header Authorization: Bearer <token>, valida la firma y 
expiración, y extrae el rol del usuario. Si el token es válido, establece 
la autenticación en el contexto de Spring Security.

La autorización por roles se implementa en SecurityConfig, donde se define 
qué roles pueden acceder a qué endpoints. Por ejemplo, solo usuarios con rol 
ADMIN pueden crear, actualizar o eliminar productos, mientras que cualquier 
usuario autenticado puede ver un producto específico.

Esta implementación es stateless (sin sesión en el servidor), lo que permite 
escalabilidad horizontal y facilita la comunicación entre microservicios.
```

---

## 📌 IE3.3.2: Desarrolla gestión de sesiones en frontend (6%)

### ✅ Qué Debes Demostrar:
- Almacenamiento del token en frontend
- Persistencia de sesión (localStorage)
- Verificación de sesión al cargar la app
- Manejo de token expirado

### 📝 Estructura de Respuesta:

```
1. ALMACENAMIENTO DEL TOKEN
   - localStorage para persistencia
   - Guardado después de login/registro
   - Estructura: token, user

2. PERSISTENCIA DE SESIÓN
   - Token persiste después de recargar página
   - Verificación al iniciar la app
   - Restauración de sesión si token válido

3. MANEJO DE TOKEN EXPIRADO
   - Interceptor detecta 401
   - Eliminación automática de token
   - Redirección a login

4. EVIDENCIAS
   - Código de almacenamiento
   - Código de verificación
   - Código de interceptor
   - Capturas de DevTools (opcional)
```

### 🔍 Evidencias a Incluir:

**1. Almacenamiento:**
```javascript
// Después de login
localStorage.setItem('token', data.data.token);
localStorage.setItem('user', JSON.stringify(data.data.user));
```

**2. Verificación al Cargar:**
```javascript
useEffect(() => {
  const token = localStorage.getItem('token');
  const user = localStorage.getItem('user');
  if (token && user) {
    setToken(token);
    setUser(JSON.parse(user));
  }
}, []);
```

**3. Manejo de 401:**
```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
  }
);
```

---

## 📌 IE3.3.5: Expone gestión de sesiones en frontend (8%)

### ✅ Qué Debes Explicar:
- **Cómo** se implementó la persistencia
- **Por qué** se eligió localStorage
- **Cómo** se maneja la expiración
- **Flujo** de gestión de sesión

### 📝 Estructura de Respuesta:

```
1. NECESIDAD DE PERSISTENCIA
   - Mantener sesión después de recargar
   - Evitar login repetido
   - Mejor experiencia de usuario

2. IMPLEMENTACIÓN CON LOCALSTORAGE
   - Almacenamiento del token JWT
   - Almacenamiento de datos del usuario
   - Ventajas: Persistencia, fácil acceso
   - Desventajas: Vulnerable a XSS (mitigado con validación)

3. VERIFICACIÓN DE SESIÓN
   - Al cargar la app, verificar token guardado
   - Si existe, restaurar sesión
   - Si no existe o expirado, redirigir a login

4. MANEJO DE EXPIRACIÓN
   - Interceptor detecta respuesta 401
   - Eliminación automática de token
   - Redirección a login
   - Mensaje informativo al usuario

5. SEGURIDAD
   - Token no se expone en URLs
   - Validación en cada request
   - Eliminación automática si expirado
```

---

## 📌 IE3.3.3: Desarrolla restricciones de acceso en frontend (6%)

### ✅ Qué Debes Demostrar:
- Rutas protegidas
- Componentes condicionales según rol
- Ocultación de funcionalidades
- Redirección si no autorizado

### 📝 Estructura de Respuesta:

```
1. RUTAS PROTEGIDAS
   - Componente ProtectedRoute
   - Verificación de autenticación
   - Verificación de rol (ADMIN)

2. COMPONENTES CONDICIONALES
   - Mostrar/ocultar según rol
   - Botones solo para ADMIN
   - Menús adaptativos

3. REDIRECCIÓN
   - No autenticado → Login
   - Sin permisos → Página de error 403

4. EVIDENCIAS
   - Código de ProtectedRoute
   - Código de componentes condicionales
   - Capturas de UI (opcional)
```

### 🔍 Evidencias a Incluir:

**1. ProtectedRoute:**
```javascript
const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { isAuthenticated, isAdmin } = useAuth();
  
  if (!isAuthenticated()) {
    return <Navigate to="/login" />;
  }
  
  if (requireAdmin && !isAdmin()) {
    return <Navigate to="/unauthorized" />;
  }
  
  return children;
};
```

**2. Componente Condicional:**
```javascript
{isAdmin() && (
  <button onClick={handleCreateProduct}>
    Crear Producto
  </button>
)}
```

---

## 📌 IE3.3.6: Explica restricciones de acceso en frontend (8%)

### ✅ Qué Debes Explicar:
- **Cómo** se implementaron las restricciones
- **Por qué** son necesarias
- **Flujo** de verificación
- **Mejores prácticas**

### 📝 Estructura de Respuesta:

```
1. NECESIDAD DE RESTRICCIONES
   - Seguridad en capas (frontend + backend)
   - Mejor UX (no mostrar opciones no disponibles)
   - Prevención de intentos no autorizados

2. IMPLEMENTACIÓN
   - Hook useAuth para estado de autenticación
   - Componente ProtectedRoute para rutas
   - Renderizado condicional en componentes
   - Verificación de rol antes de mostrar acciones

3. FLUJO DE VERIFICACIÓN
   - Usuario intenta acceder a ruta protegida
   - ProtectedRoute verifica autenticación
   - Si requiere ADMIN, verifica rol
   - Permite acceso o redirige

4. COMPONENTES CONDICIONALES
   - Botones solo visibles para ADMIN
   - Menús adaptativos según rol
   - Mensajes informativos

5. SEGURIDAD
   - Restricciones en frontend son UX, no seguridad real
   - Backend valida siempre (seguridad real)
   - Doble capa de protección
```

---

## 📝 Formato Sugerido para Cada Respuesta

### Estructura:

```
1. INTRODUCCIÓN (1-2 párrafos)
   - Qué se implementó
   - Tecnologías utilizadas

2. IMPLEMENTACIÓN TÉCNICA (2-3 párrafos)
   - Cómo se hizo
   - Código clave
   - Configuraciones

3. EVIDENCIAS (Lista)
   - Archivos modificados/creados
   - Capturas de pantalla
   - Ejemplos de código

4. JUSTIFICACIÓN (1-2 párrafos)
   - Por qué se hizo así
   - Decisiones de diseño
   - Ventajas

5. CONCLUSIÓN (1 párrafo)
   - Resumen
   - Resultado obtenido
```

---

## 🎯 Checklist General

Antes de entregar, verifica:

- [ ] Todas las evidencias están incluidas
- [ ] Código está comentado y explicado
- [ ] Capturas de pantalla son claras
- [ ] Explicaciones son técnicas pero comprensibles
- [ ] Justificaciones son sólidas
- [ ] Formato es consistente
- [ ] Referencias a archivos específicos del proyecto
- [ ] Ejemplos reales del código implementado

---

## 📚 Recursos Adicionales

### Archivos Clave del Proyecto:

**Backend:**
- `*/src/main/java/*/model/*.java` - Modelos de datos
- `*/src/main/java/*/service/*.java` - Lógica de negocio
- `*/src/main/java/*/controller/*.java` - Endpoints REST
- `*/src/main/java/*/config/SecurityConfig.java` - Seguridad
- `*/src/main/java/*/util/JwtUtil.java` - JWT
- `*/src/main/resources/application.properties` - Configuración

**Frontend (si aplica):**
- `src/services/api.js` - Servicios API
- `src/hooks/useAuth.js` - Hook de autenticación
- `src/components/ProtectedRoute.jsx` - Rutas protegidas

---

## 💡 Tips para una Buena Respuesta

1. **Sé Específico**: Menciona archivos, clases, métodos exactos
2. **Incluye Código**: Muestra ejemplos reales del proyecto
3. **Explica el "Por Qué"**: No solo el "Qué" y "Cómo"
4. **Usa Diagramas**: Si es posible, incluye diagramas de flujo
5. **Sé Técnico pero Claro**: Usa terminología correcta pero explica conceptos
6. **Muestra Resultados**: Incluye capturas de Swagger, respuestas, etc.

---

¡Buena suerte con tu evaluación! 🚀


