package com.Catalogo.Inventario.filter;

import com.Catalogo.Inventario.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Ignorar rutas de Swagger y documentación
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || 
            path.startsWith("/v3/api-docs") || 
            path.startsWith("/api-docs") ||
            path.startsWith("/swagger-ui.html") ||
            path.equals("/") ||
            path.startsWith("/swagger-resources") ||
            path.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Obtener el token del header Authorization
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // Verificar que el header Authorization existe y comienza con "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Extraer el token (después de "Bearer ")

            try {
                // Extraer el email del token
                email = jwtService.extractEmail(token);
            } catch (Exception e) {
                // Token inválido, continuar sin autenticación
                logger.warn("Error al extraer email del token: " + e.getMessage());
            }
        }

        // Si tenemos un email y no hay autenticación actual en el contexto
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Validar el token
                if (jwtService.validateToken(token)) {
                    // Obtener el rol del token
                    String role = jwtService.extractRole(token);
                    
                    // Crear la autoridad basada en el rol
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                    
                    // Crear el objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(authority)
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Establecer la autenticación en el contexto de Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Token inválido o expirado
                logger.warn("Token inválido o expirado: " + e.getMessage());
            }
        }

        // Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}

