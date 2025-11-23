package com.Gestion.Usuarios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String firstName;

    @Column(nullable = false)
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String lastName;

    @Column(unique = true, nullable = false)
    @Schema(description = "Correo electrónico único", example = "juan@demo.com")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Contraseña", example = "123456")
    private String password;

    @Column(nullable = true) 
    private String phone;

    @Column(nullable = false)
    @Schema(description = "Rol del usuario", example = "CLIENTE")
    private String role; // ADMIN, VENDEDOR, CLIENTE
}
