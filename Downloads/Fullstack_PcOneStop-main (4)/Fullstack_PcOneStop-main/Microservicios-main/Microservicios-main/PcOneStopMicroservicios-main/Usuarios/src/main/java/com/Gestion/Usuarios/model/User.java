package com.Gestion.Usuarios.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos desconocidos en el JSON
@Schema(description = "Usuario de la plataforma PcOneStop. Para registro se requieren todos los campos excepto id. Para login solo se requieren email y password.")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autogenerado (no se envía en el request)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El nombre no puede estar vacío")
    @JsonProperty("firstName")
    @Schema(description = "Nombre del usuario (obligatorio para registro)", example = "Juan")
    private String firstName;

    @Column(nullable = false)
    @NotBlank(message = "El apellido no puede estar vacío")
    @JsonProperty("lastName")
    @Schema(description = "Apellido del usuario (obligatorio para registro)", example = "Pérez")
    private String lastName;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @JsonProperty("email")
    @Schema(description = "Correo electrónico único. Formato válido requerido (ej: usuario@dominio.com). Obligatorio para registro y login.", example = "juan.perez@example.com")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @JsonProperty("password")
    @Schema(description = "Contraseña del usuario. Mínimo 8 caracteres. Obligatoria para registro y login.", example = "miPassword123")
    private String password;

    @Column(nullable = false)
    @NotBlank(message = "El rol es obligatorio")
    @JsonProperty("role")
    @Schema(description = "Rol del usuario. Valores válidos: CLIENTE (para comprar componentes) o ADMIN (para gestionar el sistema). Obligatorio solo para registro.", example = "CLIENTE", allowableValues = {"CLIENTE", "ADMIN"})
    private String role; // ADMIN, CLIENTE
}