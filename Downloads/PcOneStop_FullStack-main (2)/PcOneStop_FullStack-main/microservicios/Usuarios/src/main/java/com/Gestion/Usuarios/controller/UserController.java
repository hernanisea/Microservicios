package com.Gestion.Usuarios.controller;

import com.Gestion.Usuarios.dto.ApiResponse;
import com.Gestion.Usuarios.dto.LoginResponse;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.service.JwtService;
import com.Gestion.Usuarios.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro, login y gestión de cuenta")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario y devuelve token JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente. Se devuelve el usuario creado junto con su token JWT para autenticación inmediata."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "El email ya está registrado en el sistema. Por favor, utiliza otro email o inicia sesión."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación. Verifica que todos los campos requeridos estén correctamente completados."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor. Por favor, intenta nuevamente más tarde.")
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody User user) {
        try {
            // Verificar si el email ya existe
            User existingUser = userService.findByEmail(user.getEmail());
            if (existingUser != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(false, 409, "El email ya está registrado", null, 0L));
            }
            
            User newUser = userService.save(user);
            
            // Generar token JWT para el nuevo usuario
            String token = jwtService.generateToken(newUser.getEmail(), newUser.getId(), newUser.getRole());
            
            // Crear respuesta con usuario y token
            LoginResponse registerResponse = new LoginResponse(newUser, token);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, 201, "Usuario registrado", registerResponse, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales por email y contraseña y devuelve token JWT")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso. Se devuelve la información del usuario y el token JWT necesario para acceder a endpoints protegidos."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas. El email no existe o la contraseña es incorrecta. Verifica tus datos e intenta nuevamente."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error interno del servidor. Por favor, intenta nuevamente más tarde.")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody Map<String, String> loginData) {
        try {
            String email = loginData.get("email");
            String password = loginData.get("password");
            
            User user = userService.findByEmail(email);
            
            // Email no encontrado
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, 401, "El email ingresado no existe.", null, 0L));
            }
            
            // Verificación de contraseña
            if (!userService.checkPassword(user, password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(false, 401, "Contraseña incorrecta.", null, 0L));
            }
            
            // Generar token JWT
            String token = jwtService.generateToken(user.getEmail(), user.getId(), user.getRole());
            
            // Crear respuesta con usuario y token
            LoginResponse loginResponse = new LoginResponse(user, token);
            
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Login exitoso", loginResponse, 1L));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Obtener todos los usuarios", description = "Solo disponible para administradores", 
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente. Solo administradores pueden acceder a esta información."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "No autenticado. Debes iniciar sesión y proporcionar un token JWT válido."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Acceso denegado. Este endpoint solo está disponible para usuarios con rol de administrador.")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, "Lista de usuarios", users, (long) users.size()));
    }

    @Operation(summary = "Obtener usuario por ID", description = "Obtiene la información completa de un usuario específico")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario encontrado. Se devuelve la información completa del usuario solicitado."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado. El ID proporcionado no existe en el sistema.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        try {
            User user = userService.findById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Usuario encontrado", user, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Actualizar usuario", description = "Actualiza la información de un usuario existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente. Los cambios se han aplicado correctamente."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Error de validación. Verifica que los datos proporcionados sean válidos."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado. El ID proporcionado no existe en el sistema.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable String id, @RequestBody User userData) {
        try {
            User updatedUser = userService.update(id, userData);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Usuario actualizado", updatedUser, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Actualizar contraseña", description = "Cambia la contraseña de un usuario existente")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente. La nueva contraseña ya está activa."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado. El ID proporcionado no existe en el sistema."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al actualizar la contraseña. Por favor, intenta nuevamente más tarde.")
    })
    @PutMapping("/{id}/password")
    public ResponseEntity<ApiResponse<User>> updatePassword(
            @PathVariable String id,
            @RequestParam String newPassword
    ) {
        try {
            User updatedUser = userService.updatePassword(id, newPassword);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Contraseña actualizada", updatedUser, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Eliminar usuario", description = "Elimina permanentemente una cuenta de usuario del sistema")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cuenta eliminada exitosamente. La cuenta de usuario ha sido eliminada permanentemente."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Usuario no encontrado. El ID proporcionado no existe en el sistema."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Error al eliminar la cuenta. Por favor, intenta nuevamente más tarde.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Cuenta eliminada", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }
}
