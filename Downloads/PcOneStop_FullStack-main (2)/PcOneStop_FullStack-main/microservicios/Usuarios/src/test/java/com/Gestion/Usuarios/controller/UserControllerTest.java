package com.Gestion.Usuarios.controller;

import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.service.JwtService;
import com.Gestion.Usuarios.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== TESTS PARA REGISTER ====================

    @Test
    void testRegister_Success_ReturnsToken() throws Exception {
        // DADO: un usuario nuevo
        User newUser = new User();
        newUser.setId("user-test-001");
        newUser.setName("Test User");
        newUser.setEmail("test@test.com");
        newUser.setRole("CLIENT");
        newUser.setPassword("HASHED_PASSWORD");

        // Mock del servicio
        when(userService.findByEmail("test@test.com")).thenReturn(null);
        when(userService.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(anyString(), anyString(), anyString()))
                .thenReturn("mock-jwt-token-123");

        // Request body como JSON string para evitar problemas con WRITE_ONLY
        String requestBody = "{\"name\":\"Test User\",\"email\":\"test@test.com\",\"password\":\"123456\",\"role\":\"CLIENT\"}";

        // CUANDO/ENTONCES: el registro devuelve token
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.email").value("test@test.com"));

        // Verificar que se generó el token
        verify(jwtService).generateToken("test@test.com", "user-test-001", "CLIENT");
    }

    @Test
    void testRegister_EmailExists_ReturnsConflict() throws Exception {
        // DADO: un email que ya existe
        User existingUser = new User();
        existingUser.setEmail("existing@test.com");

        when(userService.findByEmail("existing@test.com")).thenReturn(existingUser);

        // Request body como JSON string para evitar problemas con WRITE_ONLY
        String requestBody = "{\"name\":\"Test User\",\"email\":\"existing@test.com\",\"password\":\"123456\",\"role\":\"CLIENT\"}";

        // CUANDO/ENTONCES: devuelve 409 Conflict
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("El email ya está registrado"));

        // Verificar que NO se guardó y NO se generó token
        verify(userService, never()).save(any());
        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString());
    }

    // ==================== TESTS PARA LOGIN ====================

    @Test
    void testLogin_Success_ReturnsToken() throws Exception {
        // DADO: un usuario existente con credenciales válidas
        User existingUser = new User();
        existingUser.setId("user-test-001");
        existingUser.setEmail("test@test.com");
        existingUser.setPassword("HASHED_PASSWORD");
        existingUser.setRole("CLIENT");

        when(userService.findByEmail("test@test.com")).thenReturn(existingUser);
        when(userService.checkPassword(existingUser, "123456")).thenReturn(true);
        when(jwtService.generateToken("test@test.com", "user-test-001", "CLIENT"))
                .thenReturn("mock-jwt-token-123");

        // Request body
        String requestBody = "{\"email\":\"test@test.com\",\"password\":\"123456\"}";

        // CUANDO/ENTONCES: el login devuelve token
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.user.email").value("test@test.com"));

        // Verificar que se generó el token
        verify(jwtService).generateToken("test@test.com", "user-test-001", "CLIENT");
    }

    @Test
    void testLogin_InvalidPassword_ReturnsUnauthorized() throws Exception {
        // DADO: un usuario con password incorrecto
        User existingUser = new User();
        existingUser.setEmail("test@test.com");
        existingUser.setPassword("HASHED_PASSWORD");

        when(userService.findByEmail("test@test.com")).thenReturn(existingUser);
        when(userService.checkPassword(existingUser, "wrongPassword")).thenReturn(false);

        // Request body
        String requestBody = "{\"email\":\"test@test.com\",\"password\":\"wrongPassword\"}";

        // CUANDO/ENTONCES: devuelve 401 Unauthorized
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("Contraseña incorrecta."));

        // Verificar que NO se generó token
        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_EmailNotFound_ReturnsUnauthorized() throws Exception {
        // DADO: un email que no existe
        when(userService.findByEmail("nonexistent@test.com")).thenReturn(null);

        // Request body
        String requestBody = "{\"email\":\"nonexistent@test.com\",\"password\":\"123456\"}";

        // CUANDO/ENTONCES: devuelve 401 Unauthorized
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.message").value("El email ingresado no existe."));

        // Verificar que NO se generó token
        verify(jwtService, never()).generateToken(anyString(), anyString(), anyString());
    }

    // ==================== TESTS PARA GET ALL USERS ====================
    // Nota: Security está deshabilitado en tests (application-test.properties)
    // Estos tests verifican la lógica del controlador, no la seguridad
    // Para tests de seguridad, crear tests de integración separados con security habilitado

    @Test
    void testGetAllUsers_ReturnsUsers() throws Exception {
        // DADO: usuarios en la base de datos
        User user1 = new User();
        user1.setId("user-1");
        user1.setEmail("user1@test.com");
        
        User user2 = new User();
        user2.setId("user-2");
        user2.setEmail("user2@test.com");

        when(userService.findAll()).thenReturn(java.util.Arrays.asList(user1, user2));

        // CUANDO/ENTONCES: devuelve lista de usuarios
        mockMvc.perform(get("/api/v1/auth")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
        
        verify(userService).findAll();
    }
}

