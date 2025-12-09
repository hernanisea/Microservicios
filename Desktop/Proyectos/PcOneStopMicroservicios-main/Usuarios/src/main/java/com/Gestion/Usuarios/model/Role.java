package com.Gestion.Usuarios.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa los roles del sistema")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del rol", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @Schema(description = "Nombre del rol", example = "CLIENTE", allowableValues = {"ADMIN", "VENDEDOR", "CLIENTE"})
    private String name;

    @Column(length = 255)
    @Schema(description = "Descripción del rol", example = "Usuario que realiza compras en la plataforma")
    private String description;
}

