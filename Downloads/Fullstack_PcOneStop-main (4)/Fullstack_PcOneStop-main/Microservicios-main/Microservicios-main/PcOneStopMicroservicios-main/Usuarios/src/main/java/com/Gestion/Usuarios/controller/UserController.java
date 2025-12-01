package com.Gestion.Usuarios.controller;

import com.Gestion.Usuarios.dto.ApiResponse;
import com.Gestion.Usuarios.dto.LoginResponse;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.service.UserService;
import com.Gestion.Usuarios.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación PcOneStop", description = "Gestión de usuarios, registro e inicio de sesión para clientes y administradores de la plataforma de componentes de PC")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(
        summary = "Registrar nuevo usuario en PcOneStop",
        description = "Crea una nueva cuenta en la plataforma PcOneStop. Los usuarios pueden registrarse como CLIENTE (para comprar componentes de PC) o ADMIN (para gestionar el sistema). " +
                     "El sistema valida automáticamente: formato de email válido, longitud mínima de contraseña (8 caracteres), y campos obligatorios. " +
                     "Al registrarse exitosamente, se genera un token JWT válido por 24 horas que se devuelve en la respuesta para autenticación automática. " +
                     "Este endpoint es público y no requiere autenticación previa."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Usuario registrado exitosamente. Se devuelve el token JWT para autenticación automática.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(name = "Respuesta exitosa", value = "{\"ok\": true, \"statusCode\": 201, \"message\": \"Usuario registrado\", \"data\": {\"user\": {\"id\": 1, \"firstName\": \"Juan\", \"lastName\": \"Pérez\", \"email\": \"juan@example.com\", \"role\": \"CLIENTE\"}, \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Error de validación: email inválido, contraseña muy corta (mínimo 8 caracteres), campos obligatorios faltantes, o email ya registrado",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "Email inválido", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El formato del correo no es válido\", \"data\": null, \"count\": 0}"),
                    @ExampleObject(name = "Contraseña corta", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"La contraseña debe tener al menos 8 caracteres\", \"data\": null, \"count\": 0}"),
                    @ExampleObject(name = "Email duplicado", value = "{\"ok\": false, \"statusCode\": 400, \"message\": \"El email ya está registrado\", \"data\": null, \"count\": 0}")
                }
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor durante el proceso de registro",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 500, \"message\": \"Error al procesar el registro\", \"data\": null, \"count\": 0}")
            )
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Datos del nuevo usuario. Todos los campos son obligatorios excepto el id.",
        required = true,
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = User.class),
            examples = {
                @ExampleObject(
                    name = "default",
                    summary = "Registro como Cliente (por defecto)",
                    value = "{\"firstName\":\"Juan\",\"lastName\":\"Pérez\",\"email\":\"juan.perez@example.com\",\"password\":\"miPassword123\",\"role\":\"CLIENTE\"}"
                ),
                @ExampleObject(
                    name = "Registro como Admin",
                    summary = "Ejemplo de registro de administrador",
                    value = "{\"firstName\":\"María\",\"lastName\":\"González\",\"email\":\"maria.gonzalez@pcstop.com\",\"password\":\"adminSecure2024\",\"role\":\"ADMIN\"}"
                )
            }
        )
    )
    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ApiResponse<LoginResponse>> register(HttpServletRequest request) {
        String jsonBody = null;
        try {
            // Log detallado para debugging
            logger.info("=== INICIO REGISTRO ===");
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Content-Length: {}", request.getContentLength());
            logger.info("Method: {}", request.getMethod());
            
            // LEER EL BODY DIRECTAMENTE DESDE EL REQUEST (FORMA FIRME Y EFECTIVA)
            // Esto evita que cualquier filtro consuma el body antes
            try (BufferedReader reader = request.getReader()) {
                jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                logger.info("=== JSON BODY RAW (LEÍDO DIRECTAMENTE) ===");
                logger.info("Body recibido: {}", jsonBody);
                logger.info("Body length: {}", jsonBody != null ? jsonBody.length() : 0);
            } catch (IOException e) {
                logger.error("ERROR al leer el body del request: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al leer el cuerpo de la petición: " + e.getMessage(), null, 0L));
            }
            
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("ERROR CRÍTICO: El JSON body está vacío o es NULL!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error: El cuerpo de la petición está vacío", null, 0L));
            }
            
            // Deserializar manualmente
            User user;
            try {
                user = objectMapper.readValue(jsonBody, User.class);
                logger.info("=== DESERIALIZACIÓN MANUAL ===");
                logger.info("User deserializado: {}", user);
                logger.info("firstName: '{}' (null? {})", user.getFirstName(), user.getFirstName() == null);
                logger.info("lastName: '{}' (null? {})", user.getLastName(), user.getLastName() == null);
                logger.info("email: '{}' (null? {})", user.getEmail(), user.getEmail() == null);
                logger.info("password: '{}' (null? {})", 
                        user.getPassword() != null ? "***" : null, 
                        user.getPassword() == null);
                logger.info("role: '{}' (null? {})", user.getRole(), user.getRole() == null);
            } catch (Exception e) {
                logger.error("ERROR al deserializar JSON: {}", e.getMessage(), e);
                logger.error("JSON que falló: {}", jsonBody);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al procesar el JSON: " + e.getMessage(), null, 0L));
            }
            
            if (user == null) {
                logger.error("ERROR CRÍTICO: El objeto User es NULL después de deserialización!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error: El objeto de usuario no se recibió correctamente", null, 0L));
            }
            
            logger.info("firstName: '{}' (null? {})", user.getFirstName(), user.getFirstName() == null);
            logger.info("lastName: '{}' (null? {})", user.getLastName(), user.getLastName() == null);
            logger.info("email: '{}' (null? {})", user.getEmail(), user.getEmail() == null);
            logger.info("password: '{}' (null? {}, length: {})", 
                    user.getPassword() != null ? "***" : null, 
                    user.getPassword() == null, 
                    user.getPassword() != null ? user.getPassword().length() : 0);
            logger.info("role: '{}' (null? {})", user.getRole(), user.getRole() == null);
            
            // Validación manual de campos obligatorios
            if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El nombre no puede estar vacío", null, 0L));
            }
            if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El apellido no puede estar vacío", null, 0L));
            }
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El email es obligatorio", null, 0L));
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "La contraseña es obligatoria", null, 0L));
            }
            if (user.getPassword().length() < 8) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "La contraseña debe tener al menos 8 caracteres", null, 0L));
            }
            if (user.getRole() == null || user.getRole().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El rol es obligatorio", null, 0L));
            }
            
            // Verificar si el email ya existe
            if (userService.findByEmail(user.getEmail()) != null) {
                logger.warn("Intento de registro con email ya existente: {}", user.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El email ya está registrado", null, 0L));
            }
            
            logger.info("Guardando usuario en la base de datos...");
            User newUser = userService.save(user);
            logger.info("Usuario guardado con ID: {}", newUser.getId());
            
            String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getRole(), newUser.getId());
            LoginResponse loginResponse = new LoginResponse(newUser, token);
            logger.info("=== REGISTRO EXITOSO - ID: {} ===", newUser.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, HttpStatus.CREATED.value(), "Usuario registrado", loginResponse, 1L));
        } catch (Exception e) {
            logger.error("=== ERROR AL REGISTRAR USUARIO ===", e);
            logger.error("Tipo de excepción: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa: {}", e.getCause().getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(
        summary = "Iniciar sesión en PcOneStop",
        description = "Autentica un usuario existente en PcOneStop mediante email y contraseña. Si las credenciales son correctas, " +
                     "se genera un token JWT válido por 24 horas que permite acceder a los recursos protegidos de la plataforma. " +
                     "El token incluye información del usuario (ID, email, rol) y debe incluirse en el header 'Authorization: Bearer <token>' " +
                     "en todas las peticiones que requieren autenticación. Este endpoint es público y no requiere autenticación previa."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login exitoso. Credenciales válidas, se devuelve el usuario y el token JWT.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(name = "Login exitoso", value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Login exitoso\", \"data\": {\"user\": {\"id\": 1, \"firstName\": \"Juan\", \"lastName\": \"Pérez\", \"email\": \"juan@example.com\", \"role\": \"CLIENTE\"}, \"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\"}, \"count\": 1}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas: el email no está registrado o la contraseña es incorrecta. Verifica que el email y contraseña sean correctos.",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 401, \"message\": \"Credenciales inválidas\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(mediaType = "application/json")
        )
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Credenciales de acceso. Solo se requieren email y password.",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Login",
                summary = "Ejemplo de inicio de sesión",
                value = "{\"email\":\"juan.perez@example.com\",\"password\":\"miPassword123\"}"
            )
        )
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(HttpServletRequest request) {
        String jsonBody = null;
        try {
            logger.info("=== INICIO LOGIN ===");
            logger.info("Content-Type: {}", request.getContentType());
            logger.info("Content-Length: {}", request.getContentLength());
            
            // LEER EL BODY DIRECTAMENTE DESDE EL REQUEST (MISMA SOLUCIÓN QUE REGISTER)
            try (BufferedReader reader = request.getReader()) {
                jsonBody = reader.lines().collect(Collectors.joining(System.lineSeparator()));
                logger.info("=== JSON BODY RAW (LOGIN) ===");
                logger.info("Body recibido: {}", jsonBody);
            } catch (IOException e) {
                logger.error("ERROR al leer el body del request: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al leer el cuerpo de la petición: " + e.getMessage(), null, 0L));
            }
            
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("ERROR CRÍTICO: El JSON body está vacío o es NULL!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error: El cuerpo de la petición está vacío", null, 0L));
            }
            
            // Deserializar manualmente
            User loginData;
            try {
                loginData = objectMapper.readValue(jsonBody, User.class);
                logger.info("=== DESERIALIZACIÓN LOGIN ===");
                logger.info("Email recibido: '{}'", loginData.getEmail());
                logger.info("Password recibido: '{}' (length: {})", 
                        loginData.getPassword() != null ? "***" : null,
                        loginData.getPassword() != null ? loginData.getPassword().length() : 0);
            } catch (Exception e) {
                logger.error("ERROR al deserializar JSON: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "Error al procesar el JSON: " + e.getMessage(), null, 0L));
            }
            
            // Validar que email y password no estén vacíos
            if (loginData.getEmail() == null || loginData.getEmail().trim().isEmpty()) {
                logger.error("Email vacío o null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "El email es obligatorio", null, 0L));
            }
            if (loginData.getPassword() == null || loginData.getPassword().trim().isEmpty()) {
                logger.error("Password vacío o null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>(false, 400, "La contraseña es obligatoria", null, 0L));
            }
            
            // Buscar usuario por email
            User user = userService.findByEmail(loginData.getEmail());
            logger.info("Usuario encontrado: {}", user != null ? "SÍ (ID: " + user.getId() + ")" : "NO");
            
            if (user == null) {
                logger.warn("Intento de login con email no registrado: {}", loginData.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, 401, "Credenciales inválidas", null, 0L));
            }
            
            // Verificación de contraseña encriptada
            logger.info("Verificando contraseña...");
            logger.info("Password del request (plain): '{}'", loginData.getPassword());
            logger.info("Password en BD (hashed): '{}'", user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) + "..." : "null");
            
            boolean passwordMatches = passwordEncoder.matches(loginData.getPassword(), user.getPassword());
            logger.info("Password matches: {}", passwordMatches);
            
            if (passwordMatches) {
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
                LoginResponse loginResponse = new LoginResponse(user, token);
                logger.info("=== LOGIN EXITOSO - ID: {} ===", user.getId());
                return ResponseEntity.ok(new ApiResponse<>(true, 200, "Login exitoso", loginResponse, 1L));
            } else {
                logger.warn("Contraseña incorrecta para email: {}", loginData.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, 401, "Credenciales inválidas", null, 0L));
            }
        } catch (Exception e) {
            logger.error("=== ERROR AL HACER LOGIN ===", e);
            logger.error("Tipo de excepción: {}", e.getClass().getName());
            logger.error("Mensaje: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(
        summary = "Listar todos los usuarios registrados",
        description = "Obtiene el listado completo de usuarios registrados en PcOneStop, incluyendo clientes y administradores. " +
                     "Muestra información básica de cada usuario: ID, nombre, apellido, email y rol. " +
                     "Requiere autenticación JWT. Útil para administradores que necesitan gestionar usuarios del sistema o para generar reportes."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), "Lista de usuarios", users, (long) users.size()));
    }

    @Operation(
        summary = "Obtener información de un usuario específico",
        description = "Consulta los datos completos de un usuario de PcOneStop por su ID. Incluye información personal (nombre, apellido, email) y rol. " +
                     "Útil para verificar información de perfil, validar roles, mostrar datos en el frontend o verificar permisos. " +
                     "Requiere autenticación JWT."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Usuario encontrado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado con el ID proporcionado",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": false, \"statusCode\": 404, \"message\": \"Usuario no encontrado con ID: 999\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Usuario encontrado", user, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    @Operation(
        summary = "Cambiar contraseña de usuario",
        description = "Permite a un usuario actualizar su contraseña en PcOneStop. La nueva contraseña debe tener al menos 8 caracteres " +
                     "y se encripta automáticamente usando BCrypt antes de guardarse en la base de datos. " +
                     "Requiere autenticación JWT. El usuario puede cambiar su propia contraseña o un administrador puede cambiar la de cualquier usuario."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Contraseña actualizada exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "id",
        description = "ID del usuario cuya contraseña se desea cambiar",
        required = true,
        example = "1"
    )
    @Parameter(
        name = "newPassword",
        description = "Nueva contraseña que reemplazará la actual. Debe tener al menos 8 caracteres para cumplir con los requisitos de seguridad.",
        required = true,
        example = "nuevaPassword123"
    )
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<User>> updatePassword(
            @PathVariable Long id,
            @RequestParam String newPassword
    ) {
        try {
            User updatedUser = userService.updatePassword(id, newPassword);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Contraseña actualizada", updatedUser, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(
        summary = "Eliminar cuenta de usuario",
        description = "Elimina permanentemente una cuenta de usuario de PcOneStop. Esta acción no se puede deshacer. " +
                     "Se recomienda usar con precaución. Requiere autenticación JWT."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Cuenta eliminada exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"ok\": true, \"statusCode\": 200, \"message\": \"Cuenta eliminada\", \"data\": null, \"count\": 0}")
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "No autenticado: token JWT faltante o inválido",
            content = @Content(mediaType = "application/json")
        )
    })
    @Parameter(
        name = "id",
        description = "ID del usuario a eliminar",
        required = true,
        example = "1"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Cuenta eliminada", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }
}