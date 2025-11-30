package com.Gestion.Usuarios.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // ==================== TESTS PARA save() ====================

    @Test
    public void testSave_EncriptaPassword() {
        // DADO: un usuario con password en texto plano
        User user = new User();
        user.setName("Test");
        user.setEmail("test@test.com");
        user.setPassword("123456");
        user.setRole("CLIENT");

        when(passwordEncoder.encode("123456")).thenReturn("HASH_SEGURO");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: guardamos el usuario
        User resultado = userService.save(user);

        // ENTONCES: el password debe estar encriptado
        assertEquals("HASH_SEGURO", resultado.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    public void testSave_GeneraIdSiNoViene() {
        // DADO: un usuario sin ID
        User user = new User();
        user.setName("Nuevo");
        user.setEmail("nuevo@test.com");
        user.setPassword("123456");
        user.setRole("CLIENT");

        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: guardamos el usuario
        User resultado = userService.save(user);

        // ENTONCES: se genera un ID
        assertNotNull(resultado.getId());
        assertTrue(resultado.getId().startsWith("user-"));
    }

    @Test
    public void testSave_AsignaRolClientePorDefecto() {
        // DADO: un usuario sin rol
        User user = new User();
        user.setName("Sin Rol");
        user.setEmail("sinrol@test.com");
        user.setPassword("123456");

        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: guardamos el usuario
        User resultado = userService.save(user);

        // ENTONCES: el rol es CLIENT por defecto
        assertEquals("CLIENT", resultado.getRole());
    }

    // ==================== TESTS PARA findByEmail() ====================

    @Test
    public void testFindByEmail_UsuarioExiste() {
        // DADO: un email que existe en la BD
        User mockUser = new User();
        mockUser.setId("user-client-001");
        mockUser.setEmail("juan@demo.com");
        when(userRepository.findByEmail("juan@demo.com")).thenReturn(mockUser);

        // CUANDO: buscamos por email
        User resultado = userService.findByEmail("juan@demo.com");

        // ENTONCES: retorna el usuario
        assertNotNull(resultado);
        assertEquals("juan@demo.com", resultado.getEmail());
    }

    @Test
    public void testFindByEmail_UsuarioNoExiste() {
        // DADO: un email que NO existe
        when(userRepository.findByEmail("noexiste@demo.com")).thenReturn(null);

        // CUANDO: buscamos por email
        User resultado = userService.findByEmail("noexiste@demo.com");

        // ENTONCES: retorna null
        assertNull(resultado);
    }

    // ==================== TESTS PARA findAll() ====================

    @Test
    public void testFindAll_RetornaListaDeUsuarios() {
        // DADO: 2 usuarios en la BD
        User u1 = new User();
        u1.setId("user-client-001");
        u1.setEmail("uno@test.com");
        User u2 = new User();
        u2.setId("user-client-002");
        u2.setEmail("dos@test.com");
        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        // CUANDO: obtenemos todos
        List<User> resultado = userService.findAll();

        // ENTONCES: retorna 2 usuarios
        assertEquals(2, resultado.size());
    }

    @Test
    public void testFindAll_ListaVacia() {
        // DADO: no hay usuarios
        when(userRepository.findAll()).thenReturn(Arrays.asList());

        // CUANDO: obtenemos todos
        List<User> resultado = userService.findAll();

        // ENTONCES: lista vacía
        assertTrue(resultado.isEmpty());
    }

    // ==================== TESTS PARA findById() ====================

    @Test
    public void testFindById_UsuarioExiste() {
        // DADO: un usuario con ID específico
        User mockUser = new User();
        mockUser.setId("user-client-001");
        mockUser.setEmail("encontrado@test.com");
        when(userRepository.findById("user-client-001")).thenReturn(Optional.of(mockUser));

        // CUANDO: buscamos por ID
        User resultado = userService.findById("user-client-001");

        // ENTONCES: retorna el usuario
        assertEquals("user-client-001", resultado.getId());
        assertEquals("encontrado@test.com", resultado.getEmail());
    }

    @Test
    public void testFindById_UsuarioNoExiste_LanzaExcepcion() {
        // DADO: un ID que no existe
        when(userRepository.findById("user-inexistente")).thenReturn(Optional.empty());

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.findById("user-inexistente");
        });
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    // ==================== TESTS PARA updatePassword() ====================

    @Test
    public void testUpdatePassword_CambiaYEncripta() {
        // DADO: un usuario existente
        User existente = new User();
        existente.setId("user-client-001");
        existente.setPassword("OLD_HASH");
        when(userRepository.findById("user-client-001")).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nuevaClave")).thenReturn("NUEVO_HASH");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: actualizamos password
        User resultado = userService.updatePassword("user-client-001", "nuevaClave");

        // ENTONCES: el password está encriptado con el nuevo valor
        assertEquals("NUEVO_HASH", resultado.getPassword());
        verify(userRepository).save(existente);
    }

    // ==================== TESTS PARA deleteUser() ====================

    @Test
    public void testDeleteUser_EliminaCorrectamente() {
        // CUANDO: eliminamos usuario
        userService.deleteUser("user-client-001");

        // ENTONCES: se llama al repositorio
        verify(userRepository).deleteById("user-client-001");
    }

    // ==================== TESTS PARA checkPassword() ====================

    @Test
    public void testCheckPassword_Correcto() {
        // DADO: un usuario con password encriptado
        User user = new User();
        user.setPassword("HASH_ENCRIPTADO");
        when(passwordEncoder.matches("miPassword", "HASH_ENCRIPTADO")).thenReturn(true);

        // CUANDO: verificamos el password
        boolean resultado = userService.checkPassword(user, "miPassword");

        // ENTONCES: retorna true
        assertTrue(resultado);
    }

    @Test
    public void testCheckPassword_Incorrecto() {
        // DADO: un usuario con password encriptado
        User user = new User();
        user.setPassword("HASH_ENCRIPTADO");
        when(passwordEncoder.matches("passwordIncorrecto", "HASH_ENCRIPTADO")).thenReturn(false);

        // CUANDO: verificamos con password incorrecto
        boolean resultado = userService.checkPassword(user, "passwordIncorrecto");

        // ENTONCES: retorna false
        assertFalse(resultado);
    }
}
