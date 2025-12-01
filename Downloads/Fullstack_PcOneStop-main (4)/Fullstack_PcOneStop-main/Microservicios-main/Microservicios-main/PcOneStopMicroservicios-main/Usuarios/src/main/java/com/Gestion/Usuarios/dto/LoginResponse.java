package com.Gestion.Usuarios.dto;

import com.Gestion.Usuarios.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Respuesta de login/registro que incluye el usuario y el token JWT")
public class LoginResponse {
    @Schema(description = "Datos del usuario autenticado")
    private User user;
    
    @Schema(description = "Token JWT para autenticación en peticiones posteriores", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
}

