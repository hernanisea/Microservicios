package com.Gestion.Usuarios.controller;

import com.Gestion.Usuarios.dto.ApiResponse;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para registro y login")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta de usuario")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        try {
            User newUser = userService.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, HttpStatus.CREATED.value(), "Usuario registrado", newUser, 1L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales por email y contraseña")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@RequestBody User loginData) {
        try {
            User user = userService.findByEmail(loginData.getEmail());
            if (user != null && passwordEncoder.matches(loginData.getPassword(), user.getPassword())) {
                return ResponseEntity.ok(new ApiResponse<>(true, 200, "Login exitoso", user, 1L));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(false, 401, "Credenciales inválidas", null, 0L));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }

    @Operation(summary = "Actualizar contraseña")
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

    @Operation(summary = "Eliminar usuario")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id); // Asegúrate de tener este método en UserService
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Cuenta eliminada", null, 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, e.getMessage(), null, 0L));
        }
    }
}