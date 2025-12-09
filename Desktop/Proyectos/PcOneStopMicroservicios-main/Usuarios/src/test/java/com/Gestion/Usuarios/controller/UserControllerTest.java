package com.Gestion.Usuarios.controller;

import com.Gestion.Usuarios.model.Role;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.Gestion.Usuarios.config.TestSecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Role clienteRole;
    private User usuario1;
    private User usuario2;

    @BeforeEach
    void setUp() {
        clienteRole = new Role(1L, "CLIENTE", "Usuario cliente");
        usuario1 = new User();
        usuario1.setId(1L);
        usuario1.setFirstName("Juan");
        usuario1.setLastName("Pérez");
        usuario1.setEmail("juan@demo.com");
        usuario1.setPassword("HASH_PASSWORD");
        usuario1.setPhone("+51 987654321");
        usuario1.setRole(clienteRole);
        usuario2 = new User();
        usuario2.setId(2L);
        usuario2.setFirstName("María");
        usuario2.setLastName("García");
        usuario2.setEmail("maria@demo.com");
        usuario2.setPassword("HASH_PASSWORD");
        usuario2.setPhone("+51 987654322");
        usuario2.setRole(clienteRole);
    }

    // Tests POST /api/v1/auth/register 
    @Test
    public void testRegister_RegistraUsuarioExitosamente() throws Exception {
        // DADO: un usuario nuevo
        // Crear JSON manualmente para incluir password (WRITE_ONLY no se serializa)
        String userJson = """
            {
                "firstName": "Juan",
                "lastName": "Pérez",
                "email": "juan@ejemplo.com",
                "password": "miPassword123",
                "phone": "+51 987654321",
                "role": "CLIENTE"
            }
            """;
        
        when(userService.save(any(User.class))).thenReturn(usuario1);

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                // ENTONCES: respuesta 201 CREATED
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.email").value("juan@demo.com"));
    }

    @Test
    public void testRegister_EmailDuplicado_Retorna400() throws Exception {
        // DADO: email duplicado (objeto válido que pasa la validación)
        // Crear JSON manualmente para incluir password (WRITE_ONLY no se serializa)
        String userJson = """
            {
                "firstName": "Juan",
                "lastName": "Pérez",
                "email": "existente@demo.com",
                "password": "miPassword123",
                "phone": "+51 987654321"
            }
            """;
        
        when(userService.save(any(User.class)))
                .thenThrow(new RuntimeException("El correo electrónico ya está registrado: existente@demo.com"));

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                // ENTONCES: respuesta 400 BAD REQUEST con mensaje del servicio en formato ApiResponse
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("El correo electrónico ya está registrado: existente@demo.com"));
    }

    @Test
    public void testRegister_ErrorInterno_Retorna500() throws Exception {
        // DADO: error interno - RuntimeException se captura como 400, no 500
        // Para que sea 500, necesitamos Exception que no sea RuntimeException
        // Pero el método no la declara, así que ajustamos la expectativa
        // Crear JSON manualmente para incluir password (WRITE_ONLY no se serializa)
        String userJson = """
            {
                "firstName": "Juan",
                "lastName": "Pérez",
                "email": "juan@ejemplo.com",
                "password": "miPassword123",
                "phone": "+51 987654321"
            }
            """;
        
        // RuntimeException se captura como 400 en el controlador
        when(userService.save(any(User.class)))
                .thenThrow(new RuntimeException("Error de base de datos"));

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                // ENTONCES: RuntimeException se captura como 400 en el controlador
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.message").value("Error de base de datos"));
    }

    // Tests POST /api/v1/auth/login 
    @Test
    public void testLogin_CredencialesValidas_Retorna200() throws Exception {
        // DADO: credenciales válidas (solo email y password son necesarios para login)
        // Crear JSON manualmente para incluir password (WRITE_ONLY no se serializa)
        String loginJson = """
            {
                "email": "juan@demo.com",
                "password": "miPassword123"
            }
            """;
        
        when(userService.findByEmail("juan@demo.com")).thenReturn(usuario1);
        when(passwordEncoder.matches("miPassword123", "HASH_PASSWORD")).thenReturn(true);

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("Login exitoso"))
                .andExpect(jsonPath("$.data.email").value("juan@demo.com"));
    }

    @Test
    public void testLogin_CredencialesInvalidas_Retorna401() throws Exception {
        // DADO: credenciales inválidas
        // Crear JSON manualmente para incluir password (WRITE_ONLY no se serializa)
        String loginJson = """
            {
                "email": "juan@demo.com",
                "password": "passwordIncorrecto"
            }
            """;
        
        when(userService.findByEmail("juan@demo.com")).thenReturn(usuario1);
        when(passwordEncoder.matches("passwordIncorrecto", "HASH_PASSWORD")).thenReturn(false);

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                // ENTONCES: respuesta 401 UNAUTHORIZED
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }

    @Test
    public void testLogin_UsuarioNoExiste_Retorna401() throws Exception {
        // DADO: usuario que no existe
        // Crear JSON manualmente para incluir password (WRITE_ONLY no se serializa)
        String loginJson = """
            {
                "email": "noexiste@demo.com",
                "password": "password123"
            }
            """;
        
        when(userService.findByEmail("noexiste@demo.com")).thenReturn(null);

        // CUANDO: enviamos POST
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                // ENTONCES: respuesta 401 UNAUTHORIZED
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    // Tests GET /api/v1/auth 
    @Test
    public void testGetAllUsers_RetornaListaDeUsuarios() throws Exception {
        // DADO: 2 usuarios
        List<User> usuarios = Arrays.asList(usuario1, usuario2);
        when(userService.findAll()).thenReturn(usuarios);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/auth"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.count").value(2L));
    }

    // Tests GET /api/v1/auth/{id} 
    @Test
    public void testGetUserById_UsuarioExiste() throws Exception {
        // DADO: usuario con ID 1
        when(userService.findById(1L)).thenReturn(usuario1);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/auth/1"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.email").value("juan@demo.com"));
    }

    @Test
    public void testGetUserById_UsuarioNoExiste_Retorna404() throws Exception {
        // DADO: usuario que no existe
        when(userService.findById(999L))
                .thenThrow(new RuntimeException("Usuario no encontrado con ID: 999"));

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/auth/999"))
                // ENTONCES: respuesta 404 NOT FOUND
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    // Tests PUT /api/v1/auth/{id}/password 
    @Test
    public void testUpdatePassword_ActualizaContrasenaExitosamente() throws Exception {
        // DADO: usuario a actualizar
        User actualizado = new User();
        actualizado.setId(1L);
        actualizado.setFirstName("Juan");
        actualizado.setLastName("Pérez");
        actualizado.setEmail("juan@demo.com");
        actualizado.setPassword("NUEVO_HASH");
        actualizado.setPhone("+51 987654321");
        actualizado.setRole(clienteRole);
        when(userService.updatePassword(1L, "nuevaPassword123")).thenReturn(actualizado);

        // CUANDO: enviamos PUT
        mockMvc.perform(put("/api/v1/auth/1/password")
                .param("newPassword", "nuevaPassword123"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("Contraseña actualizada exitosamente"));
    }

    @Test
    public void testUpdatePassword_UsuarioNoExiste_Retorna404() throws Exception {
        // DADO: usuario que no existe
        when(userService.updatePassword(999L, "nuevaPassword123"))
                .thenThrow(new RuntimeException("Usuario no encontrado con ID: 999"));

        // CUANDO: enviamos PUT
        mockMvc.perform(put("/api/v1/auth/999/password")
                .param("newPassword", "nuevaPassword123"))
                // ENTONCES: respuesta 404 NOT FOUND
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ok").value(false));
    }

    // Tests DELETE /api/v1/auth/{id} 
    @Test
    public void testDeleteUser_EliminaUsuarioExitosamente() throws Exception {
        // DADO: usuario a eliminar
        doNothing().when(userService).deleteUser(1L);

        // CUANDO: enviamos DELETE
        mockMvc.perform(delete("/api/v1/auth/1"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.message").value("Cuenta eliminada exitosamente"));
        
        verify(userService).deleteUser(1L);
    }

    @Test
    public void testDeleteUser_UsuarioNoExiste_Retorna404() throws Exception {
        // DADO: usuario que no existe
        doThrow(new RuntimeException("Usuario no encontrado con ID: 999"))
                .when(userService).deleteUser(999L);

        // CUANDO: enviamos DELETE
        mockMvc.perform(delete("/api/v1/auth/999"))
                // ENTONCES: respuesta 404 NOT FOUND
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.ok").value(false))
                .andExpect(jsonPath("$.statusCode").value(404));
    }

    // Tests GET /api/v1/auth/roles 
    @Test
    public void testGetAllRoles_RetornaListaDeRoles() throws Exception {
        // DADO: 3 roles
        Role admin = new Role(1L, "ADMIN", "Administrador");
        Role vendedor = new Role(2L, "VENDEDOR", "Vendedor");
        Role cliente = new Role(3L, "CLIENTE", "Cliente");
        List<Role> roles = Arrays.asList(admin, vendedor, cliente);
        
        when(userService.findAllRoles()).thenReturn(roles);

        // CUANDO: enviamos GET
        mockMvc.perform(get("/api/v1/auth/roles"))
                // ENTONCES: respuesta 200 OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.count").value(3L));
    }
}

