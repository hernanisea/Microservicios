# 📦 Dependencias del Proyecto PcOneStop Microservicios

## 📋 Resumen por Microservicio

| Dependencia | Usuarios | Inventario | Pagos | Calificaciones |
|------------|:--------:|:----------:|:-----:|:--------------:|
| **spring-boot-starter-validation** | ✅ | ✅ | ✅ | ✅ |
| **spring-boot-starter-security** | ✅ | ✅ | ✅ | ❌ |
| **jjwt-api** | ✅ | ✅ | ✅ | ❌ |
| **jjwt-impl** | ✅ | ✅ | ✅ | ❌ |
| **jjwt-jackson** | ✅ | ✅ | ✅ | ❌ |
| **springdoc-openapi** | ✅ | ✅ | ✅ | ✅ |
| **spring-boot-starter-data-jpa** | ✅ | ✅ | ✅ | ✅ |
| **spring-boot-starter-web** | ✅ | ✅ | ✅ | ✅ |
| **spring-boot-starter-webflux** | ❌ | ✅ | ✅ | ✅ |
| **spring-boot-starter-actuator** | ✅ | ❌ | ❌ | ❌ |
| **mysql-connector-j** | ✅ | ✅ | ✅ | ✅ |
| **lombok** | ✅ | ✅ | ✅ | ✅ |
| **spring-boot-starter-test** | ✅ | ✅ | ✅ | ✅ |
| **reactor-test** | ✅ | ✅ | ✅ | ✅ |

---

## 🔧 Dependencias Detalladas

### 1. **spring-boot-starter-validation** 
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Presente en:** Todos los microservicios  
**¿Qué hace?**
- Proporciona validación de datos usando anotaciones Bean Validation (JSR-303/JSR-380)
- Permite usar anotaciones como `@NotNull`, `@NotBlank`, `@Min`, `@Max`, `@Email`, `@Size`, etc.
- Valida automáticamente los objetos `@RequestBody` y `@RequestParam`
- Genera mensajes de error personalizados cuando la validación falla

**Ejemplo de uso:**
```java
@NotBlank(message = "El email es obligatorio")
@Email(message = "El email debe ser válido")
private String email;
```

---

### 2. **spring-boot-starter-security**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Presente en:** Usuarios, Inventario, Pagos  
**¿Qué hace?**
- Framework de seguridad para aplicaciones Spring
- Proporciona autenticación y autorización
- Permite configurar reglas de acceso a endpoints
- Integra con JWT para autenticación basada en tokens
- Protege endpoints contra acceso no autorizado

**Uso en el proyecto:**
- Configuración de `SecurityFilterChain` para definir qué rutas son públicas/privadas
- Filtros JWT personalizados para validar tokens
- Autorización basada en roles (ADMIN, USER)

---

### 3. **jjwt-api** (JSON Web Token API)
**Versión:** 0.12.3  
**Presente en:** Usuarios, Inventario, Pagos  
**¿Qué hace?**
- API para crear, validar y parsear tokens JWT
- Proporciona clases e interfaces para trabajar con JWT
- Permite generar tokens firmados con algoritmos como HS256, RS256, etc.
- Extrae información del token (claims, expiración, etc.)

**Uso en el proyecto:**
- Generación de tokens al hacer login
- Validación de tokens en requests entrantes
- Extracción de información del usuario desde el token

---

### 4. **jjwt-impl** (JSON Web Token Implementation)
**Versión:** 0.12.3  
**Scope:** runtime  
**Presente en:** Usuarios, Inventario, Pagos  
**¿Qué hace?**
- Implementación concreta de la API de JWT
- Contiene la lógica real para crear y validar tokens
- Solo se necesita en tiempo de ejecución (runtime), no en tiempo de compilación

---

### 5. **jjwt-jackson** (JSON Web Token Jackson Support)
**Versión:** 0.12.3  
**Scope:** runtime  
**Presente en:** Usuarios, Inventario, Pagos  
**¿Qué hace?**
- Integración de JWT con Jackson (biblioteca de serialización JSON)
- Permite serializar/deserializar objetos Java a/desde JSON dentro de los tokens JWT
- Facilita incluir objetos complejos como claims en los tokens

---

### 6. **springdoc-openapi-starter-webmvc-ui**
**Versión:** 2.7.0 (Usuarios, Pagos), 2.6.0 (Inventario, Calificaciones)  
**Presente en:** Todos los microservicios  
**¿Qué hace?**
- Genera documentación automática de la API usando OpenAPI 3.0
- Proporciona interfaz Swagger UI interactiva
- Permite probar endpoints directamente desde el navegador
- Genera documentación a partir de anotaciones como `@Operation`, `@ApiResponse`, `@Schema`
- Incluye botón "Authorize" para autenticación JWT en Swagger UI

**Acceso:** `http://localhost:PUERTO/swagger-ui.html`

---

### 7. **spring-boot-starter-data-jpa**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Presente en:** Todos los microservicios  
**¿Qué hace?**
- Integración con JPA (Java Persistence API) y Hibernate
- Permite trabajar con bases de datos relacionales usando ORM (Object-Relational Mapping)
- Proporciona repositorios que extienden `JpaRepository`
- Maneja transacciones automáticamente
- Permite usar anotaciones como `@Entity`, `@Table`, `@Column`, `@Id`, `@GeneratedValue`

**Uso en el proyecto:**
- Definición de entidades (`User`, `Product`, `Order`, `Review`)
- Repositorios que extienden `JpaRepository`
- Consultas automáticas y personalizadas

---

### 8. **spring-boot-starter-web**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Presente en:** Todos los microservicios  
**¿Qué hace?**
- Framework web para crear aplicaciones REST
- Incluye Spring MVC para manejar requests HTTP
- Permite crear controladores REST con `@RestController` y `@RequestMapping`
- Maneja serialización/deserialización JSON automáticamente
- Incluye servidor embebido Tomcat

**Uso en el proyecto:**
- Controladores REST (`@RestController`, `@GetMapping`, `@PostMapping`, etc.)
- Manejo de requests HTTP (GET, POST, PUT, DELETE)
- Respuestas JSON automáticas

---

### 9. **spring-boot-starter-webflux**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Presente en:** Inventario, Pagos, Calificaciones  
**¿Qué hace?**
- Framework reactivo para aplicaciones web
- Permite programación asíncrona y no bloqueante
- Útil para comunicación entre microservicios de forma reactiva
- Incluye `WebClient` para hacer requests HTTP asíncronos

**Nota:** Aunque está incluida, en este proyecto se usa principalmente Spring MVC (síncrono). WebFlux podría usarse para comunicación entre microservicios en el futuro.

---

### 10. **spring-boot-starter-actuator**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Presente en:** Solo Usuarios  
**¿Qué hace?**
- Proporciona endpoints de monitoreo y gestión de la aplicación
- Permite verificar el estado de salud de la aplicación (`/actuator/health`)
- Expone métricas, información del sistema, configuración, etc.
- Útil para monitoreo en producción

**Endpoints comunes:**
- `/actuator/health` - Estado de salud
- `/actuator/info` - Información de la aplicación
- `/actuator/metrics` - Métricas de la aplicación

---

### 11. **mysql-connector-j**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Scope:** runtime  
**Presente en:** Todos los microservicios  
**¿Qué hace?**
- Driver JDBC oficial de MySQL
- Permite que la aplicación Java se conecte a bases de datos MySQL
- Solo se necesita en tiempo de ejecución (runtime)
- Se configura en `application.properties` con la URL de conexión

**Configuración típica:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/nombre_bd
spring.datasource.username=usuario
spring.datasource.password=contraseña
```

---

### 12. **lombok**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Scope:** optional  
**Presente en:** Todos los microservicios  
**¿Qué hace?**
- Reduce código boilerplate (repetitivo) en Java
- Genera automáticamente getters, setters, constructores, `toString()`, `equals()`, `hashCode()`
- Anotaciones comunes:
  - `@Data` - Genera getters, setters, `toString()`, `equals()`, `hashCode()`
  - `@NoArgsConstructor` - Constructor sin argumentos
  - `@AllArgsConstructor` - Constructor con todos los argumentos
  - `@Getter` / `@Setter` - Solo getters o setters
  - `@Builder` - Patrón Builder

**Ejemplo:**
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    // Lombok genera automáticamente getters, setters, constructores, etc.
}
```

---

### 13. **spring-boot-starter-test**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Scope:** test  
**Presente en:** Todos los microservicios  
**¿Qué hace?**
- Incluye todas las dependencias necesarias para testing
- Incluye JUnit 5, Mockito, AssertJ, Hamcrest, etc.
- Permite escribir tests unitarios e integración
- Incluye `@SpringBootTest` para tests de integración
- Incluye `MockitoExtension` para tests con mocks

**Uso en el proyecto:**
- Tests unitarios de servicios (`UserServiceTest`, `ProductServiceTest`, etc.)
- Mocks de repositorios y dependencias
- Aserciones con JUnit 5

---

### 14. **reactor-test**
**Versión:** Incluida en Spring Boot 3.5.7/3.5.8  
**Scope:** test  
**Presente en:** Todos los microservicios  
**¿Qué hace?**
- Utilidades de testing para programación reactiva
- Permite testear código que usa `Mono` y `Flux` (Project Reactor)
- Útil para testear `WebClient` y código reactivo

**Nota:** Aunque está incluida, en este proyecto se usa principalmente código síncrono. Esta dependencia podría ser útil si se implementa comunicación reactiva entre microservicios.

---

## 🔌 Plugins de Maven

### 1. **maven-compiler-plugin**
**¿Qué hace?**
- Compila el código Java
- Configurado para usar Java 21
- Incluye configuración para procesar anotaciones de Lombok

### 2. **spring-boot-maven-plugin**
**¿Qué hace?**
- Permite empaquetar la aplicación como JAR ejecutable
- Excluye Lombok del JAR final (solo se necesita en tiempo de compilación)
- Permite ejecutar la aplicación con `mvn spring-boot:run`

---

## 📊 Versiones Clave

- **Spring Boot:** 3.5.7 (Usuarios, Inventario, Pagos) / 3.5.8 (Calificaciones)
- **Java:** 21
- **JWT (jjwt):** 0.12.3
- **SpringDoc OpenAPI:** 2.7.0 (Usuarios, Pagos) / 2.6.0 (Inventario, Calificaciones)

---

## 🎯 Dependencias por Categoría

### **Seguridad y Autenticación**
- `spring-boot-starter-security` - Framework de seguridad
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - Manejo de tokens JWT

### **Persistencia de Datos**
- `spring-boot-starter-data-jpa` - ORM con JPA/Hibernate
- `mysql-connector-j` - Driver de MySQL

### **API REST y Web**
- `spring-boot-starter-web` - Framework web REST
- `spring-boot-starter-webflux` - Framework reactivo (opcional)

### **Validación y Documentación**
- `spring-boot-starter-validation` - Validación de datos
- `springdoc-openapi-starter-webmvc-ui` - Documentación Swagger/OpenAPI

### **Utilidades**
- `lombok` - Reducción de código boilerplate
- `spring-boot-starter-actuator` - Monitoreo (solo Usuarios)

### **Testing**
- `spring-boot-starter-test` - Framework de testing
- `reactor-test` - Testing reactivo (opcional)

---

## ⚠️ Notas Importantes

1. **Spring Boot Parent:** Todos los microservicios heredan de `spring-boot-starter-parent`, lo que proporciona versiones compatibles de todas las dependencias.

2. **Scope `runtime`:** Dependencias como `mysql-connector-j` y `jjwt-impl` solo se necesitan en tiempo de ejecución, no en compilación.

3. **Scope `test`:** Dependencias como `spring-boot-starter-test` solo se incluyen cuando se ejecutan tests.

4. **Scope `optional`:** Lombok es opcional porque solo se necesita durante la compilación para generar código.

5. **Diferencias entre microservicios:**
   - **Usuarios** tiene `spring-boot-starter-actuator` (monitoreo)
   - **Calificaciones** NO tiene seguridad/JWT (endpoints públicos)
   - **Inventario, Pagos, Calificaciones** tienen `spring-boot-starter-webflux` (aunque no se usa activamente)

---

## 🔍 Cómo Verificar Dependencias

Para ver todas las dependencias transitivas (incluidas indirectamente):

```bash
mvn dependency:tree
```

Para ver solo las dependencias directas:

```bash
mvn dependency:tree -Dincludes=*
```


