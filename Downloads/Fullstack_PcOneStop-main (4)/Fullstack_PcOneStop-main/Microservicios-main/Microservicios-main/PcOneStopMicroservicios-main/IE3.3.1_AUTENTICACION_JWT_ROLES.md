# IE3.3.1: Autenticación con Roles y JWT - Resumen

## 🎯 Objetivo
Implementar autenticación de usuarios en el backend utilizando roles para asegurar que solo los usuarios autorizados puedan acceder a ciertos recursos, utilizando autenticación basada en tokens (JWT).

---

## ✅ Implementación en el Proyecto

### 1. **Generación del Token con Rol** (Microservicio Usuarios)

**Archivo:** `Usuarios/src/main/java/com/Gestion/Usuarios/util/JwtUtil.java`

```java
public String generateToken(String email, String role, Long userId) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", role);        // ADMIN o CLIENTE
    claims.put("userId", userId);
    return createToken(claims, email);
}
```

**¿Qué hace?**
- Al hacer login/registro, se genera un token JWT que **incluye el rol del usuario** (ADMIN o CLIENTE)
- El rol se almacena dentro del token como un "claim" (dato)

**Ejemplo en `UserController.java`:**
```java
// Después de validar credenciales
String token = jwtUtil.generateToken(
    user.getEmail(),    // "admin@test.com"
    user.getRole(),     // "ADMIN" o "CLIENTE"
    user.getId()        // 1
);
```

---

### 2. **Validación del Token en Cada Request** (Todos los Microservicios)

**Archivo:** `*/config/JwtAuthenticationFilter.java`

```java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    // 1. Extraer token del header
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        
        // 2. Validar token (firma y expiración)
        if (jwtUtil.validateToken(token)) {
            // 3. Extraer el rol del token
            String role = jwtUtil.extractRole(token);  // "ADMIN" o "CLIENTE"
            
            // 4. Crear autoridad con prefijo ROLE_ (requerido por Spring Security)
            String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);
            
            // 5. Establecer autenticación en el contexto de Spring Security
            UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(email, null, Collections.singletonList(authority));
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
}
```

**¿Qué hace?**
- Intercepta **cada request HTTP** antes de llegar al controlador
- Extrae y valida el token JWT del header `Authorization: Bearer <token>`
- Si es válido, extrae el **rol** del token y establece la autenticación en Spring Security
- Permite que Spring Security sepa quién es el usuario y qué rol tiene

---

### 3. **Autorización Basada en Roles** (Control de Acceso)

**Archivo:** `*/config/SecurityConfig.java`

#### Ejemplo en Inventario:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    return http
        .authorizeHttpRequests(auth -> auth
            // Rutas públicas (no requieren autenticación)
            .requestMatchers("GET", "/api/v1/products/{id}").permitAll()
            .requestMatchers("GET", "/api/v1/products/offers").permitAll()
            
            // Rutas que SOLO ADMIN puede acceder
            .requestMatchers("GET", "/api/v1/products").hasRole("ADMIN")        // Listar todos
            .requestMatchers("POST", "/api/v1/products").hasRole("ADMIN")       // Crear producto
            .requestMatchers("PUT", "/api/v1/products/**").hasRole("ADMIN")     // Actualizar
            .requestMatchers("DELETE", "/api/v1/products/**").hasRole("ADMIN")  // Eliminar
            
            // Otras rutas requieren autenticación (cualquier rol)
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

**¿Qué hace?**
- Define qué endpoints son **públicos** (acceso sin token)
- Define qué endpoints requieren rol **ADMIN** (solo administradores)
- Define qué endpoints requieren **cualquier autenticación** (ADMIN o CLIENTE)
- Spring Security verifica automáticamente el rol del usuario antes de permitir acceso

---

### 4. **Extracción del Rol del Token**

**Archivo:** `*/util/JwtUtil.java`

```java
public String extractRole(String token) {
    return extractClaim(token, claims -> claims.get("role", String.class));
}
```

**¿Qué hace?**
- Lee el claim `"role"` del token JWT
- Retorna el rol como string: `"ADMIN"` o `"CLIENTE"`

---

## 🔄 Flujo Completo de Autenticación y Autorización

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. USUARIO HACE LOGIN                                            │
│    POST /api/v1/auth/login                                      │
│    {email: "admin@test.com", password: "..."}                   │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 2. USUARIOS GENERA TOKEN CON ROL                                 │
│    jwtUtil.generateToken(email, "ADMIN", userId)                 │
│    Token contiene: {role: "ADMIN", userId: 1, email: "..."}    │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 3. CLIENTE ENVÍA REQUEST CON TOKEN                               │
│    GET /api/v1/products                                          │
│    Authorization: Bearer <token>                                 │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 4. JwtAuthenticationFilter VALIDA TOKEN                          │
│    - Extrae token del header                                     │
│    - Valida firma y expiración                                   │
│    - Extrae rol: "ADMIN"                                        │
│    - Establece autenticación en SecurityContext                 │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 5. SecurityConfig VERIFICA ROL                                    │
│    - Endpoint requiere: hasRole("ADMIN")                        │
│    - Usuario tiene rol: "ADMIN" ✅                              │
│    - PERMITE ACCESO                                              │
└────────────────────────────┬──────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│ 6. ProductController.list() SE EJECUTA                          │
│    - Retorna lista de productos                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📋 Ejemplos Concretos

### Ejemplo 1: Usuario ADMIN accede a recurso protegido

**Request:**
```http
GET http://localhost:8082/api/v1/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjEsInN1YiI6ImFkbWluQHRlc3QuY29tIn0...
```

**Proceso:**
1. `JwtAuthenticationFilter` extrae token → Valida → Extrae `role: "ADMIN"`
2. `SecurityConfig` verifica `hasRole("ADMIN")` → ✅ **PERMITE**
3. `ProductController.list()` se ejecuta → Retorna productos

**Resultado:** ✅ **200 OK** con lista de productos

---

### Ejemplo 2: Usuario CLIENTE intenta acceder a recurso solo ADMIN

**Request:**
```http
GET http://localhost:8082/api/v1/products
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiQ0xJRU5URSIsInVzZXJJZCI6Miwic3ViIjoiY2xpZW50ZUB0ZXN0LmNvbSJ9...
```

**Proceso:**
1. `JwtAuthenticationFilter` extrae token → Valida → Extrae `role: "CLIENTE"`
2. `SecurityConfig` verifica `hasRole("ADMIN")` → ❌ **RECHAZA** (es CLIENTE, no ADMIN)
3. `ProductController.list()` **NO se ejecuta**

**Resultado:** ❌ **403 Forbidden** - "Access Denied"

---

### Ejemplo 3: Usuario sin token intenta acceder

**Request:**
```http
GET http://localhost:8082/api/v1/products
(No Authorization header)
```

**Proceso:**
1. `JwtAuthenticationFilter` no encuentra token
2. `SecurityConfig` verifica autenticación → ❌ **NO AUTENTICADO**
3. `ProductController.list()` **NO se ejecuta**

**Resultado:** ❌ **403 Forbidden** o **401 Unauthorized**

---

## 🎯 Resumen de la Implementación

| Componente | Función | Ubicación |
|------------|---------|-----------|
| **JwtUtil.generateToken()** | Genera token JWT con rol incluido | `Usuarios/util/JwtUtil.java` |
| **JwtAuthenticationFilter** | Valida token y extrae rol en cada request | `*/config/JwtAuthenticationFilter.java` |
| **SecurityConfig** | Define qué roles pueden acceder a qué endpoints | `*/config/SecurityConfig.java` |
| **JwtUtil.extractRole()** | Extrae el rol del token | `*/util/JwtUtil.java` |

---

## ✅ Cumplimiento del Requisito IE3.3.1

| Requisito | Implementación |
|-----------|----------------|
| ✅ Autenticación de usuarios | Token JWT generado en login/registro |
| ✅ Roles | Rol incluido en el token (ADMIN/CLIENTE) |
| ✅ Acceso restringido | `SecurityConfig` con `hasRole("ADMIN")` |
| ✅ Autenticación basada en tokens | JWT con firma criptográfica y expiración |
| ✅ Validación en cada request | `JwtAuthenticationFilter` intercepta todos los requests |

---

## 🔑 Puntos Clave

1. **El rol se incluye en el token JWT** al momento de generarlo
2. **Cada microservicio valida el token** independientemente usando el mismo `JwtUtil`
3. **Spring Security verifica el rol** antes de permitir acceso a endpoints protegidos
4. **El token es stateless** - no se almacena en el servidor, toda la información está en el token
5. **La clave secreta debe ser la misma** en todos los microservicios para validar tokens

---

## 📝 Configuración Necesaria

**En `application.properties` de cada microservicio:**
```properties
jwt.secret=mySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLong
jwt.expiration=86400000  # 24 horas
```

**Importante:** La clave secreta (`jwt.secret`) debe ser **la misma** en todos los microservicios para que puedan validar tokens generados por otros.

---

## 🎓 Conclusión

El proyecto implementa correctamente la autenticación basada en tokens JWT con roles:

- ✅ **Generación:** Token JWT incluye rol del usuario (ADMIN/CLIENTE)
- ✅ **Validación:** Cada request valida el token automáticamente
- ✅ **Autorización:** Solo usuarios con rol ADMIN pueden acceder a recursos protegidos
- ✅ **Seguridad:** Token firmado criptográficamente y con expiración

**Ejemplo práctico:** Solo usuarios con rol `ADMIN` pueden crear, actualizar o eliminar productos en el microservicio de Inventario, mientras que cualquier usuario autenticado puede ver un producto específico.


