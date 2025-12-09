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
@Schema(description = "Entidad que representa un usuario del sistema")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID autogenerado del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre no puede estar vacío")
    @Schema(description = "Nombre del usuario", example = "Juan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El apellido no puede estar vacío")
    @Schema(description = "Apellido del usuario", example = "Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @Column(unique = true, nullable = false, length = 255)
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del correo no es válido")
    @Schema(description = "Correo electrónico único", example = "juan@demo.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // Solo acepta en POST/PUT, nunca en GET
    @Schema(description = "Contraseña (mínimo 8 caracteres)", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Column(length = 20)
    @Schema(description = "Número de teléfono (opcional)", example = "+51 987654321")
    private String phone;

    // --- RELACIÓN CON ROLE (FK normalizada) ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @Schema(description = "Rol asignado al usuario")
    private Role role;

    // --- Campo transitorio para recibir el nombre del rol desde el frontend ---
    @Transient
    @JsonProperty("role")
    @Schema(description = "Nombre del rol para registro (ADMIN, VENDEDOR, CLIENTE)", example = "CLIENTE")
    private String roleName;

    // Getter personalizado para serialización JSON
    @JsonProperty("role")
    public String getRoleName() {
        return role != null ? role.getName() : roleName;
    }

    // Setter para deserialización desde el frontend
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
