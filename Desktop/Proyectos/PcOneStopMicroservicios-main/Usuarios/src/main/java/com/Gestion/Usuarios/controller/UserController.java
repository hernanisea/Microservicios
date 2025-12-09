package com.Gestion.Usuarios.controller;

import com.Gestion.Usuarios.dto.ApiResponse;
import com.Gestion.Usuarios.model.Role;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación y Usuarios", description = "Endpoints para registro, login y gestión de cuentas de usuario")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Registro
    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea una nueva cuenta de usuario con validaciones. La contraseña se encripta automáticamente con BCrypt."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Usuario registrado exitosamente",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos (validación fallida)"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Datos del usuario a registrar",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = User.class),
                    examples = @ExampleObject(
                        name = "Ejemplo de registro",
                        value = """
                            {
                                "firstName": "Juan",
                                "lastName": "Pérez",
                                "email": "juan@ejemplo.com",
                                "password": "miPassword123",
                                "phone": "+51 987654321",
                                "role": "CLIENTE"
                            }
                            """
                    )
                )
            )
            @Valid @RequestBody User user
    ) {
        try {
            User newUser = userService.save(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, HttpStatus.CREATED.value(), "Usuario registrado exitosamente", newUser, 1L));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error interno: " + e.getMessage(), null, 0L));
        }
    }

    // Login
    @Operation(
        summary = "Iniciar sesión",
        description = "Valida credenciales por email y contraseña. La contraseña se compara contra el hash BCrypt almacenado. " +
                      "Retorna 401 si el usuario no existe o la contraseña no coincide."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login exitoso - credenciales válidas"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas - usuario no existe o contraseña incorrecta"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor durante la validación"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credenciales de acceso",
                content = @Content(
                    examples = @ExampleObject(
                        name = "Ejemplo de login",
                        value = """
                            {
                                "email": "juan@ejemplo.com",
                                "password": "miPassword123"
                            }
                            """
                    )
                )
            )
            @RequestBody User loginData
    ) {
        try {
            User user = userService.findByEmail(loginData.getEmail());
            
            if (user != null && passwordEncoder.matches(loginData.getPassword(), user.getPassword())) {
                return ResponseEntity.ok(new ApiResponse<>(true, 200, "Login exitoso", user, 1L));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, 401, "Credenciales inválidas", null, 0L));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    // Listar todos
    @Operation(
        summary = "Listar todos los usuarios",
        description = "Obtiene la lista completa de usuarios registrados. Solo para uso administrativo."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de usuarios obtenida exitosamente"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), "Lista de usuarios obtenida", users, (long) users.size()));
    }

    // Obtener por ID
    @Operation(
        summary = "Obtener usuario por ID",
        description = "Busca y devuelve un usuario específico por su identificador único."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id
    ) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Usuario encontrado", user, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    // Actualizar contraseña
    @Operation(
        summary = "Actualizar contraseña",
        description = "Permite cambiar la contraseña de un usuario existente. La nueva contraseña se encripta automáticamente con BCrypt."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Contraseña actualizada exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado - el ID proporcionado no existe en el sistema"
        )
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<User>> updatePassword(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nueva contraseña (mínimo 8 caracteres)", example = "nuevaPass123", required = true)
            @RequestParam String newPassword
    ) {
        try {
            User updatedUser = userService.updatePassword(id, newPassword);
            return ResponseEntity.ok(new ApiResponse<>(
                    true, HttpStatus.OK.value(), "Contraseña actualizada exitosamente", updatedUser, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    // Eliminar usuario
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina permanentemente una cuenta de usuario del sistema. Esta operación no se puede deshacer."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Usuario eliminado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado - el ID proporcionado no existe en el sistema"
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "ID del usuario a eliminar", example = "1", required = true)
            @PathVariable Long id
    ) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Cuenta eliminada exitosamente", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    // Listar roles
    @Operation(
        summary = "Listar roles disponibles",
        description = "Obtiene todos los roles disponibles en el sistema (ADMIN, VENDEDOR, CLIENTE). " +
                      "Útil para poblar dropdowns en formularios de registro."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Lista de roles obtenida exitosamente"
        )
    })
    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = userService.findAllRoles();
        return ResponseEntity.ok(new ApiResponse<>(
                true, HttpStatus.OK.value(), "Roles disponibles", roles, (long) roles.size()));
    }

    // Manejo de errores de validación
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());
        
        String errorMessage = errors.isEmpty() 
            ? "Error de validación en los datos de entrada" 
            : String.join(", ", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, 400, errorMessage, null, 0L));
    }
}
