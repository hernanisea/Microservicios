package com.Pedidos.Pagos.controller;

import com.Pedidos.Pagos.dto.ApiResponse;
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

@RestControllerAdvice(basePackages = "com.Pedidos.Pagos.controller")
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
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        // Obtener el primer mensaje de error para el mensaje principal
        String firstError = errors.values().iterator().hasNext() 
            ? errors.values().iterator().next() 
            : "Error de validación";
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, 400, firstError, errors, (long) errors.size()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        // El basePackages ya limita el alcance a solo los controladores de la API
        // Si por alguna razón llegamos aquí con una ruta de Swagger, simplemente no manejamos la excepción
        if (request != null && !shouldHandleException(request)) {
            // Dejar que Spring maneje la excepción normalmente (no retornamos nada)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        String errorMessage = "Error al procesar los datos del pedido";
        
        // Si es un error de formato (como intentar convertir string a Long)
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
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, 400, errorMessage, null, 0L));
    }

    // Eliminado @ExceptionHandler(Exception.class) genérico para no interferir con Swagger
    // Solo manejamos excepciones específicas arriba
}

