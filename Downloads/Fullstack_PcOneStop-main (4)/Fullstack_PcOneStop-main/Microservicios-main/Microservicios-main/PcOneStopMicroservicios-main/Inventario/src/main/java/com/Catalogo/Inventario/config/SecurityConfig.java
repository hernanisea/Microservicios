package com.Catalogo.Inventario.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Configurar CORS PRIMERO
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Deshabilitar CSRF para APIs REST
            .csrf(AbstractHttpConfigurer::disable)
            // Configurar sesión stateless (para JWT)
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Configurar autorización
            .authorizeHttpRequests(auth -> auth
                    // Permitir OPTIONS para CORS preflight
                    .requestMatchers("OPTIONS", "/**").permitAll()
                    // Endpoints públicos
                    .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    .requestMatchers("GET", "/api/v1/products/{id}").permitAll() // GET de producto por ID público (para ver detalles)
                    .requestMatchers("GET", "/api/v1/products/offers").permitAll() // GET de productos en oferta público
                    .requestMatchers("GET", "/api/v1/reports/count/**").permitAll() // GET de conteo de reportes público
                    .requestMatchers("GET", "/api/reports/count/**").permitAll() // GET de conteo de reportes público (ruta alternativa)
                    // Endpoints que requieren rol ADMIN
                    .requestMatchers("GET", "/api/v1/products").hasRole("ADMIN") // GET de todos los productos requiere ADMIN
                    .requestMatchers("POST", "/api/v1/products").hasRole("ADMIN") // POST crear producto requiere ADMIN
                    .requestMatchers("PUT", "/api/v1/products/**").hasRole("ADMIN") // PUT actualizar producto requiere ADMIN
                    .requestMatchers("DELETE", "/api/v1/products/**").hasRole("ADMIN") // DELETE eliminar producto requiere ADMIN
                    // Endpoints que requieren autenticación (cualquier rol autenticado)
                    .requestMatchers("POST", "/api/v1/reports").authenticated() // POST de reportes requiere autenticación
                    .requestMatchers("POST", "/api/reports").authenticated() // POST de reportes requiere autenticación (ruta alternativa)
                    .requestMatchers("POST", "/api/products/**/reports").authenticated() // POST de reportes por producto requiere autenticación
                    .requestMatchers("POST", "/api/products/*/reports").authenticated() // POST de reportes por producto requiere autenticación (patrón alternativo)
                    .requestMatchers("GET", "/api/v1/reports").authenticated() // GET de todos los reportes requiere autenticación
                    .requestMatchers("GET", "/api/reports").authenticated() // GET de todos los reportes requiere autenticación (ruta alternativa)
                    .anyRequest().authenticated()
            )
            // Agregar el filtro JWT DESPUÉS de la configuración de autorización
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173",
            "http://127.0.0.1:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

