package com.Gestion.Usuarios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // <--- IMPORTANTE: Para las validaciones
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autogenerado", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre no puede estar vacío") // <--- Validación
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "El apellido no puede estar vacío") // <--- Validación
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del correo no es válido") // <--- Validación de formato
    @Schema(description = "Correo electrónico único", example = "juan@demo.com")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") // <--- Validación de longitud
    @Schema(description = "Contraseña", example = "123456")
    private String password;

    @Column(nullable = true)
    // No ponemos @NotBlank porque definiste que puede ser opcional (nullable = true)
    // Pero si quisieras validar formato cuando sí lo envían, podrías usar @Pattern
    private String phone;

    @Column(nullable = false)
    @NotBlank(message = "El rol es obligatorio") // <--- Validación
    @Schema(description = "Rol del usuario", example = "CLIENTE")
    private String role; // ADMIN, VENDEDOR, CLIENTE
}