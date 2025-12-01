package com.Catalogo.Inventario.config;

import com.Catalogo.Inventario.util.JwtUtil;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // IMPORTANTE: Saltar el filtro para rutas públicas
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Permitir OPTIONS para CORS preflight
        if ("OPTIONS".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Saltar el filtro para rutas públicas
        if (path.startsWith("/swagger-ui") || 
            path.equals("/swagger-ui.html") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/api-docs") ||
            path.contains("swagger-ui") ||
            path.contains("api-docs") ||
            (path.matches("/api/v1/products/\\d+") && "GET".equals(method)) || // GET producto por ID específico
            (path.equals("/api/v1/products/offers") && "GET".equals(method)) || // GET productos en oferta
            (path.startsWith("/api/v1/reports/count") && "GET".equals(method)) ||
            (path.startsWith("/api/reports/count") && "GET".equals(method))) {
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
                email = jwtUtil.extractEmail(token);
            } catch (Exception e) {
                // Token inválido, continuar sin autenticación
                logger.warn("Error al extraer email del token: " + e.getMessage());
            }
        }

        // Si tenemos un email y no hay autenticación actual en el contexto
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Validar el token
                if (jwtUtil.validateToken(token)) {
                    // Obtener el rol del token
                    String role = jwtUtil.extractRole(token);
                    
                    // Crear la autoridad basada en el rol (asegurar que tenga el prefijo ROLE_)
                    String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);
                    
                    // Crear el objeto de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(authority)
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Establecer la autenticación en el contexto de Spring Security
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.debug("Usuario autenticado: " + email + " con rol: " + authorityName);
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

