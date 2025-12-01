# 🔐 Funcionamiento del Token JWT en el Código

## 📋 Índice
1. [Visión General](#visión-general)
2. [Generación del Token](#generación-del-token)
3. [Validación del Token](#validación-del-token)
4. [Flujo Completo de Autenticación](#flujo-completo-de-autenticación)
5. [Estructura del Token](#estructura-del-token)
6. [Componentes Clave](#componentes-clave)

---

## 🎯 Visión General

El sistema usa **JWT (JSON Web Tokens)** para autenticación y autorización entre microservicios. El token se genera en el microservicio **Usuarios** y se valida en **Usuarios**, **Inventario** y **Pagos**.

### Flujo Simplificado:
```
1. Usuario hace login/registro → Usuarios genera token JWT
2. Cliente envía token en header: Authorization: Bearer <token>
3. Cada microservicio valida el token automáticamente
4. Si es válido, extrae información (email, rol, userId) y permite acceso
```

---

## 🔨 Generación del Token

### Ubicación: `Usuarios/src/main/java/com/Gestion/Usuarios/util/JwtUtil.java`

#### 1. **Método Principal: `generateToken()`**

```java
public String generateToken(String email, String role, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);        // Ej: "ADMIN" o "CLIENTE"
    claims.put("userId", userId);    // ID del usuario en la BD
    return createToken(claims, email);  // email es el "subject"
}
```

**¿Qué hace?**
- Crea un mapa de "claims" (datos) que incluyen:
  - `role`: Rol del usuario (ADMIN, CLIENTE)
  - `userId`: ID del usuario en la base de datos
- Llama a `createToken()` para construir el token JWT

#### 2. **Método Privado: `createToken()`**

```java
private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
            .claims(claims)                                    // Datos personalizados
            .subject(subject)                                   // Email del usuario
            .issuedAt(new Date(System.currentTimeMillis()))     // Fecha de emisión
            .expiration(new Date(System.currentTimeMillis() + expiration))  // Fecha de expiración
            .signWith(getSigningKey())                          // Firma con clave secreta
            .compact();                                         // Genera el string final
}
```

**¿Qué hace?**
- Construye el token JWT usando la biblioteca `jjwt`
- **Claims**: Datos personalizados (role, userId)
- **Subject**: Email del usuario (identificador principal)
- **Issued At**: Fecha/hora de creación
- **Expiration**: Fecha/hora de expiración (24 horas por defecto)
- **Sign With**: Firma el token con una clave secreta (HMAC SHA-256)

#### 3. **Clave Secreta: `getSigningKey()`**

```java
@Value("${jwt.secret:mySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong}")
private String secret;

private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
}
```

**¿Qué hace?**
- Lee la clave secreta desde `application.properties`
- Convierte el string a una clave criptográfica HMAC SHA-256
- Esta clave se usa para **firmar** y **verificar** el token

**Configuración en `application.properties`:**
```properties
jwt.secret=mySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong
jwt.expiration=86400000  # 24 horas en milisegundos
```

### 📍 Dónde se Genera el Token

#### En `UserController.java` - Método `register()`:

```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<LoginResponse>> register(...) {
    // ... validaciones y guardado del usuario ...
    
    User newUser = userService.save(user);
    
    // 🔑 GENERACIÓN DEL TOKEN
    String token = jwtUtil.generateToken(
        newUser.getEmail(),    // Email del usuario
        newUser.getRole(),     // Rol (ADMIN o CLIENTE)
        newUser.getId()        // ID del usuario
    );
    
    LoginResponse loginResponse = new LoginResponse(newUser, token);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(true, 201, "Usuario registrado", loginResponse, 1L));
}
```

#### En `UserController.java` - Método `login()`:

```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(...) {
    // ... validación de credenciales ...
    
    if (passwordEncoder.matches(loginData.getPassword(), user.getPassword())) {
        // 🔑 GENERACIÓN DEL TOKEN
        String token = jwtUtil.generateToken(
            user.getEmail(),   // Email del usuario
            user.getRole(),    // Rol (ADMIN o CLIENTE)
            user.getId()       // ID del usuario
        );
        
        LoginResponse loginResponse = new LoginResponse(user, token);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Login exitoso", loginResponse, 1L));
    }
}
```

---

## ✅ Validación del Token

### Ubicación: `JwtAuthenticationFilter.java` (en cada microservicio)

El filtro se ejecuta **automáticamente** antes de cada request HTTP.

### Flujo de Validación:

```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    
    // 1️⃣ SALTAR RUTAS PÚBLICAS
    String path = request.getRequestURI();
    if (path.startsWith("/swagger-ui") || 
        path.startsWith("/api/v1/auth") ||  // Solo en Usuarios
        path.matches("/api/v1/products/\\d+") && "GET".equals(method)) {  // Solo en Inventario
        filterChain.doFilter(request, response);
        return;  // No valida token, permite acceso
    }
    
    // 2️⃣ EXTRAER TOKEN DEL HEADER
    String authHeader = request.getHeader("Authorization");
    String token = null;
    
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        token = authHeader.substring(7);  // Quita "Bearer " y obtiene el token
        
        // 3️⃣ EXTRAER EMAIL DEL TOKEN
        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            logger.warn("Error al extraer email del token: " + e.getMessage());
            // Token inválido, continúa sin autenticación
        }
    }
    
    // 4️⃣ VALIDAR Y AUTENTICAR
    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
            // Validar que el token no esté expirado
            if (jwtUtil.validateToken(token)) {
                // Extraer el rol del token
                String role = jwtUtil.extractRole(token);
                
                // Crear autoridad con prefijo ROLE_ (requerido por Spring Security)
                String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);
                
                // Crear objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        email,           // Principal (identificador)
                        null,            // Credentials (no se usa con JWT)
                        Collections.singletonList(authority)  // Roles/permisos
                    );
                
                // Establecer en el contexto de Spring Security
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            logger.warn("Token inválido o expirado: " + e.getMessage());
        }
    }
    
    // 5️⃣ CONTINUAR CON EL SIGUIENTE FILTRO
    filterChain.doFilter(request, response);
}
```

### Métodos de Validación en `JwtUtil.java`:

#### 1. **`validateToken(String token)`**

```java
public Boolean validateToken(String token) {
    try {
        return !isTokenExpired(token);  // Verifica que no esté expirado
    } catch (Exception e) {
        return false;  // Si hay error al parsear, es inválido
    }
}

private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
}
```

**¿Qué hace?**
- Verifica que el token no esté expirado
- Si hay error al parsear el token, retorna `false`

#### 2. **`extractAllClaims(String token)`**

```java
private Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())  // Verifica la firma con la clave secreta
            .build()
            .parseSignedClaims(token)     // Parsea y valida el token
            .getPayload();                 // Retorna los claims (datos)
}
```

**¿Qué hace?**
- **Parsea** el token JWT
- **Verifica la firma** usando la misma clave secreta que se usó para firmarlo
- Si la firma es inválida, lanza una excepción
- Retorna los **claims** (datos) del token

#### 3. **Métodos de Extracción:**

```java
// Extraer email (subject)
public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
}

// Extraer rol
public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
}

// Extraer userId
public Long extractUserId(String token) {
    return extractClaim(token, claims -> {
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    });
}

// Extraer fecha de expiración
public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
}
```

---

## 🔄 Flujo Completo de Autenticación

### Escenario 1: Usuario se Registra

```
┌─────────┐                    ┌──────────┐                    ┌─────────┐
│ Cliente │                    │ Usuarios │                    │   BD    │
└────┬────┘                    └────┬─────┘                    └────┬────┘
     │                              │                                │
     │ 1. POST /api/v1/auth/register│                                │
     │    {email, password, role}    │                                │
     ├──────────────────────────────>│                                │
     │                              │                                │
     │                              │ 2. Guardar usuario en BD      │
     │                              ├───────────────────────────────>│
     │                              │                                │
     │                              │ 3. Usuario guardado (ID: 5)    │
     │                              │<───────────────────────────────┤
     │                              │                                │
     │                              │ 4. Generar token JWT           │
     │                              │    jwtUtil.generateToken(      │
     │                              │      email="juan@test.com",    │
     │                              │      role="CLIENTE",           │
     │                              │      userId=5                  │
     │                              │    )                            │
     │                              │                                │
     │ 5. Respuesta con token       │                                │
     │    {user: {...}, token: "..."}│                                │
     │<──────────────────────────────┤                                │
     │                              │                                │
```

### Escenario 2: Usuario hace Login

```
┌─────────┐                    ┌──────────┐                    ┌─────────┐
│ Cliente │                    │ Usuarios │                    │   BD    │
└────┬────┘                    └────┬─────┘                    └────┬────┘
     │                              │                                │
     │ 1. POST /api/v1/auth/login   │                                │
     │    {email, password}          │                                │
     ├──────────────────────────────>│                                │
     │                              │                                │
     │                              │ 2. Buscar usuario por email    │
     │                              ├───────────────────────────────>│
     │                              │                                │
     │                              │ 3. Usuario encontrado          │
     │                              │<───────────────────────────────┤
     │                              │                                │
     │                              │ 4. Verificar contraseña        │
     │                              │    passwordEncoder.matches()   │
     │                              │                                │
     │                              │ 5. Generar token JWT           │
     │                              │    jwtUtil.generateToken(...)   │
     │                              │                                │
     │ 6. Respuesta con token       │                                │
     │    {user: {...}, token: "..."}│                                │
     │<──────────────────────────────┤                                │
     │                              │                                │
```

### Escenario 3: Cliente Accede a Recurso Protegido

```
┌─────────┐                    ┌──────────┐                    ┌─────────┐
│ Cliente │                    │Inventario │                    │   BD    │
└────┬────┘                    └────┬─────┘                    └────┬────┘
     │                              │                                │
     │ 1. GET /api/v1/products      │                                │
     │    Authorization: Bearer <token>│                                │
     ├──────────────────────────────>│                                │
     │                              │                                │
     │                              │ 2. JwtAuthenticationFilter     │
     │                              │    - Extrae token del header   │
     │                              │    - Valida token              │
     │                              │    - Extrae email, role        │
     │                              │    - Establece autenticación   │
     │                              │                                │
     │                              │ 3. SecurityConfig              │
     │                              │    - Verifica hasRole("ADMIN") │
     │                              │    - Si es ADMIN, permite      │
     │                              │    - Si no, rechaza (403)      │
     │                              │                                │
     │                              │ 4. ProductController.list()    │
     │                              │    - Obtiene productos         │
     │                              ├───────────────────────────────>│
     │                              │                                │
     │                              │ 5. Lista de productos          │
     │                              │<───────────────────────────────┤
     │                              │                                │
     │ 6. Respuesta con productos   │                                │
     │    [{id: 1, name: "GPU"...}] │                                │
     │<──────────────────────────────┤                                │
     │                              │                                │
```

---

## 📦 Estructura del Token

Un token JWT tiene **3 partes** separadas por puntos (`.`):

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInN1YiI6Imp1YW5AdGVzdC5jb20iLCJpYXQiOjE3MDAwMDAwMDAsImV4cCI6MTcwMDA4NjQwMH0.signature
```

### 1. **Header** (Primera parte)
```json
{
  "alg": "HS256",    // Algoritmo de firma (HMAC SHA-256)
  "typ": "JWT"       // Tipo de token
}
```

### 2. **Payload** (Segunda parte - Claims)
```json
{
  "role": "ADMIN",                    // Rol del usuario
  "userId": 1,                        // ID del usuario
  "sub": "juan@test.com",             // Subject (email)
  "iat": 1700000000,                 // Issued At (fecha de emisión)
  "exp": 1700086400                   // Expiration (fecha de expiración)
}
```

### 3. **Signature** (Tercera parte)
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret_key
)
```

**¿Por qué es seguro?**
- La firma garantiza que el token **no fue modificado**
- Solo quien tiene la clave secreta puede **crear** o **modificar** tokens válidos
- Si alguien modifica el payload, la firma no coincidirá y el token será rechazado

---

## 🧩 Componentes Clave

### 1. **JwtUtil.java** (Utilidad para JWT)

**Ubicación:** `Usuarios/util/`, `Inventario/util/`, `Pagos/util/`

**Responsabilidades:**
- ✅ Generar tokens (solo en Usuarios)
- ✅ Validar tokens
- ✅ Extraer información del token (email, role, userId, expiration)

**Métodos principales:**
- `generateToken(email, role, userId)` - Genera un nuevo token
- `validateToken(token)` - Valida que el token no esté expirado
- `extractEmail(token)` - Extrae el email del token
- `extractRole(token)` - Extrae el rol del token
- `extractUserId(token)` - Extrae el ID del usuario
- `extractExpiration(token)` - Extrae la fecha de expiración

### 2. **JwtAuthenticationFilter.java** (Filtro de Autenticación)

**Ubicación:** `Usuarios/config/`, `Inventario/config/`, `Pagos/config/`

**Responsabilidades:**
- ✅ Interceptar cada request HTTP
- ✅ Extraer el token del header `Authorization`
- ✅ Validar el token
- ✅ Establecer la autenticación en el contexto de Spring Security

**Flujo:**
1. Se ejecuta **antes** de cada request
2. Verifica si la ruta es pública (Swagger, login, etc.)
3. Si es protegida, extrae y valida el token
4. Si es válido, establece la autenticación en `SecurityContextHolder`
5. Continúa con el siguiente filtro

### 3. **SecurityConfig.java** (Configuración de Seguridad)

**Ubicación:** `Usuarios/config/`, `Inventario/config/`, `Pagos/config/`

**Responsabilidades:**
- ✅ Configurar qué rutas son públicas/privadas
- ✅ Configurar autorización basada en roles
- ✅ Registrar el `JwtAuthenticationFilter`
- ✅ Configurar CORS

**Ejemplo (Inventario):**
```java
.authorizeHttpRequests(auth -> auth
    // Rutas públicas
    .requestMatchers("GET", "/api/v1/products/{id}").permitAll()
    .requestMatchers("GET", "/api/v1/products/offers").permitAll()
    
    // Rutas que requieren ADMIN
    .requestMatchers("GET", "/api/v1/products").hasRole("ADMIN")
    .requestMatchers("POST", "/api/v1/products").hasRole("ADMIN")
    .requestMatchers("PUT", "/api/v1/products/**").hasRole("ADMIN")
    .requestMatchers("DELETE", "/api/v1/products/**").hasRole("ADMIN")
    
    // Otras rutas requieren autenticación
    .anyRequest().authenticated()
)
```

### 4. **UserController.java** (Generación de Tokens)

**Ubicación:** `Usuarios/controller/`

**Responsabilidades:**
- ✅ Generar tokens cuando el usuario se registra o hace login
- ✅ Devolver el token en la respuesta

**Métodos:**
- `register()` - Genera token después de registrar usuario
- `login()` - Genera token después de validar credenciales

---

## 🔒 Seguridad

### ¿Cómo se Protege el Token?

1. **Firma Criptográfica:**
   - El token se firma con una clave secreta (HMAC SHA-256)
   - Si alguien modifica el token, la firma no coincidirá y será rechazado

2. **Expiración:**
   - Los tokens expiran después de 24 horas (configurable)
   - Después de expirar, el usuario debe hacer login nuevamente

3. **Validación en Cada Request:**
   - Cada microservicio valida el token independientemente
   - No hay sesión en el servidor (stateless)

4. **Clave Secreta:**
   - La clave secreta debe ser **misma** en todos los microservicios
   - Debe ser **larga y aleatoria** (mínimo 256 bits)
   - Se almacena en `application.properties` (en producción, usar variables de entorno)

### ⚠️ Buenas Prácticas

1. **Nunca exponer la clave secreta** en el código fuente
2. **Usar HTTPS** en producción para proteger el token en tránsito
3. **Rotar la clave secreta** periódicamente
4. **Validar el token** en cada request (ya implementado)
5. **No almacenar información sensible** en el token (solo datos necesarios)

---

## 📝 Ejemplo de Uso

### 1. Cliente hace Login:

**Request:**
```http
POST http://localhost:8081/api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "ok": true,
  "statusCode": 200,
  "message": "Login exitoso",
  "data": {
    "user": {
      "id": 1,
      "firstName": "Admin",
      "lastName": "User",
      "email": "admin@test.com",
      "role": "ADMIN"
    },
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInN1YiI6ImFkbWluQHRlc3QuY29tIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.signature"
  },
  "count": 1
}
```

### 2. Cliente Accede a Recurso Protegido:

**Request:**
```http
GET http://localhost:8082/api/v1/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInN1YiI6ImFkbWluQHRlc3QuY29tIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.signature
```

**Proceso Interno:**
1. `JwtAuthenticationFilter` intercepta el request
2. Extrae el token del header `Authorization`
3. Valida el token (firma y expiración)
4. Extrae `role: "ADMIN"` y `email: "admin@test.com"`
5. Establece autenticación en `SecurityContextHolder`
6. `SecurityConfig` verifica `hasRole("ADMIN")` → ✅ Permite acceso
7. `ProductController.list()` se ejecuta y retorna productos

**Response:**
```json
{
  "ok": true,
  "statusCode": 200,
  "message": "Lista de productos",
  "data": [
    {
      "id": 1,
      "name": "GPU RTX 4070",
      "brand": "Nvidia",
      "price": 700.0,
      "stock": 10
    }
  ],
  "count": 1
}
```

### 3. Cliente sin Token o Token Inválido:

**Request:**
```http
GET http://localhost:8082/api/v1/products
(No Authorization header)
```

**Response:**
```http
HTTP/1.1 403 Forbidden
```

**O si el token está expirado:**
```http
HTTP/1.1 401 Unauthorized
```

---

## 🎓 Resumen

1. **Generación:** El token se genera en `Usuarios` cuando el usuario se registra o hace login
2. **Estructura:** El token contiene email, role, userId, fecha de emisión y expiración
3. **Validación:** Cada microservicio valida el token automáticamente usando `JwtAuthenticationFilter`
4. **Autorización:** `SecurityConfig` controla qué roles pueden acceder a qué endpoints
5. **Seguridad:** El token está firmado criptográficamente y expira después de 24 horas

---

## 🔍 Archivos Clave

| Archivo | Ubicación | Función |
|---------|-----------|---------|
| `JwtUtil.java` | `Usuarios/util/` | Genera tokens |
| `JwtUtil.java` | `Inventario/util/`, `Pagos/util/` | Valida tokens |
| `JwtAuthenticationFilter.java` | `*/config/` | Filtro de autenticación |
| `SecurityConfig.java` | `*/config/` | Configuración de seguridad |
| `UserController.java` | `Usuarios/controller/` | Endpoints de login/registro |
| `application.properties` | `*/resources/` | Configuración (jwt.secret, jwt.expiration) |

---

¿Tienes alguna pregunta específica sobre el funcionamiento del token JWT? 🤔


