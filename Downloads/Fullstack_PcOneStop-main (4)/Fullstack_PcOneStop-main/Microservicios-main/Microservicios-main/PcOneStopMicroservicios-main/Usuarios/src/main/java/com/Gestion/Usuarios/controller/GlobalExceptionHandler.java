package com.Gestion.Usuarios.controller;

import com.Gestion.Usuarios.dto.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.Gestion.Usuarios.controller")
public class GlobalExceptionHandler {

    // Excluir rutas de Swagger del manejo de excepciones
    private boolean shouldHandleException(HttpServletRequest request) {
        if (request == null) return true;
        String path = request.getRequestURI();
        if (path == null) return true;
        return !path.startsWith("/swagger-ui") && 
               !path.startsWith("/v3/api-docs") && 
               !path.startsWith("/api-docs") &&
               !path.startsWith("/swagger-resources") &&
               !path.startsWith("/webjars") &&
               !path.startsWith("/configuration");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        // El basePackages ya limita el alcance a solo los controladores de la API
        // Si por alguna razón llegamos aquí con una ruta de Swagger, simplemente no manejamos la excepción
        if (request != null && !shouldHandleException(request)) {
            // Dejar que Spring maneje la excepción normalmente (no retornamos nada)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        
        // Logging detallado para debugging
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logger.error("=== ERROR DE VALIDACIÓN ===");
        logger.error("Request URI: {}", request != null ? request.getRequestURI() : "null");
        logger.error("Request Method: {}", request != null ? request.getMethod() : "null");
        logger.error("Content-Type: {}", request != null ? request.getContentType() : "null");
        logger.error("Número de errores: {}", ex.getBindingResult().getErrorCount());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValueObj = ((FieldError) error).getRejectedValue();
            String rejectedValue = rejectedValueObj != null ? rejectedValueObj.toString() : "null";
            errors.put(fieldName, errorMessage);
            logger.error("Campo '{}': {} (valor rechazado: '{}')", fieldName, errorMessage, rejectedValue);
        });
        
        // Obtener el primer mensaje de error para el mensaje principal
        String firstError = errors.values().iterator().hasNext() 
            ? errors.values().iterator().next() 
            : "Error de validación";
        
        logger.error("=== FIN ERROR DE VALIDACIÓN ===");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, 400, firstError, errors, (long) errors.size()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        // Si la excepción ocurre en una ruta de Swagger, no la manejamos aquí.
        if (request != null && !shouldHandleException(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, 500, "Error interno del servidor al procesar la documentación de la API.", null, 0L));
        }
        
        // Logging detallado para debugging
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logger.error("=== ERROR DE DESERIALIZACIÓN JSON ===");
        logger.error("Request URI: {}", request != null ? request.getRequestURI() : "null");
        logger.error("Request Method: {}", request != null ? request.getMethod() : "null");
        logger.error("Content-Type: {}", request != null ? request.getContentType() : "null");
        logger.error("Content-Length: {}", request != null ? request.getContentLength() : "null");
        logger.error("Mensaje de error: {}", ex.getMessage());
        logger.error("Causa: {}", ex.getCause() != null ? ex.getCause().getClass().getName() : "null");
        if (ex.getCause() != null) {
            logger.error("Mensaje de la causa: {}", ex.getCause().getMessage());
        }
        logger.error("Stack trace completo:", ex);
        logger.error("=== FIN ERROR DE DESERIALIZACIÓN JSON ===");
        
        String errorMessage = "Error al procesar los datos del usuario";
        Throwable cause = ex.getCause();
        
        if (cause instanceof InvalidFormatException) {
            InvalidFormatException formatException = (InvalidFormatException) cause;
            String fieldName = formatException.getPath().stream()
                    .map(ref -> ref.getFieldName())
                    .reduce((first, second) -> second)
                    .orElse("campo desconocido");
            String value = formatException.getValue() != null ? formatException.getValue().toString() : "null";
            String targetType = formatException.getTargetType() != null ? formatException.getTargetType().getSimpleName() : "tipo desconocido";
            errorMessage = String.format("El campo '%s' tiene un valor inválido '%s'. Se esperaba un %s.", 
                    fieldName, value, targetType);
        } else if (cause != null) {
            errorMessage = "Error de formato en los datos: " + cause.getMessage();
        } else {
            errorMessage = "Error al leer el cuerpo de la petición. Asegúrate de que el Content-Type sea 'application/json' y que el JSON sea válido.";
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, 400, errorMessage, null, 0L));
    }

    // Eliminado @ExceptionHandler(Exception.class) genérico para no interferir con Swagger
    // Solo manejamos excepciones específicas arriba
}

