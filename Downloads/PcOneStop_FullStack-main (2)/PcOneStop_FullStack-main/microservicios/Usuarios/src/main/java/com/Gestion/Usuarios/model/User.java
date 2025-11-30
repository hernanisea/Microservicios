package com.Gestion.Usuarios.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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
    @Schema(description = "ID único del usuario (UUID)", example = "user-client-01")
    private String id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre no puede estar vacío")
    @Schema(description = "Nombre del usuario", example = "Pedro")
    private String name;

    @Column(name = "last_name", nullable = true)
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Schema(description = "Correo electrónico único", example = "cliente@gmail.com")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // No exponer password en respuestas
    @Schema(description = "Contraseña", example = "123456")
    private String password;

    @Column(nullable = false)
    @NotBlank(message = "El rol es obligatorio")
    @Schema(description = "Rol del usuario (ADMIN o CLIENT)", example = "CLIENT")
    private String role; // ADMIN, CLIENT
}
