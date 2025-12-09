package com.Gestion.Usuarios.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Gestion.Usuarios.model.Role;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.repository.RoleRepository;
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
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // Tests save() 
    @Test
    public void testSave_EncriptaPasswordYAsignaRol() {
        // DADO: un usuario con password en texto plano
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("12345678");
        user.setRoleName("CLIENTE");

        Role clienteRole = new Role();
        clienteRole.setId(1L);
        clienteRole.setName("CLIENTE");

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("HASH_SEGURO");
        when(roleRepository.findByName("CLIENTE")).thenReturn(Optional.of(clienteRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: guardamos el usuario
        User resultado = userService.save(user);

        // ENTONCES: el password debe estar encriptado y el rol asignado
        assertEquals("HASH_SEGURO", resultado.getPassword());
        assertEquals("CLIENTE", resultado.getRole().getName());
        verify(userRepository).save(user);
    }

    @Test
    public void testSave_EmailDuplicado_LanzaExcepcion() {
        // DADO: un email que ya existe
        User user = new User();
        user.setEmail("existente@test.com");
        user.setPassword("12345678");

        when(userRepository.existsByEmail("existente@test.com")).thenReturn(true);

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.save(user);
        });
        assertTrue(ex.getMessage().contains("ya está registrado"));
    }

    @Test
    public void testSave_RolPorDefectoEsCliente() {
        // DADO: un usuario sin rol especificado
        User user = new User();
        user.setEmail("nuevo@test.com");
        user.setPassword("12345678");
        // No se especifica roleName

        Role clienteRole = new Role();
        clienteRole.setId(3L);
        clienteRole.setName("CLIENTE");

        when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
        when(passwordEncoder.encode("12345678")).thenReturn("HASH");
        when(roleRepository.findByName("CLIENTE")).thenReturn(Optional.of(clienteRole));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: guardamos
        User resultado = userService.save(user);

        // ENTONCES: se asigna rol CLIENTE por defecto
        assertEquals("CLIENTE", resultado.getRole().getName());
    }

    // Tests findByEmail() 
    @Test
    public void testFindByEmail_UsuarioExiste() {
        // DADO: un email que existe en la BD
        User mockUser = new User();
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

    // Tests findAll() 
    @Test
    public void testFindAll_RetornaListaDeUsuarios() {
        // DADO: 2 usuarios en la BD
        User u1 = new User();
        u1.setEmail("uno@test.com");
        User u2 = new User();
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

    // Tests findById() 
    @Test
    public void testFindById_UsuarioExiste() {
        // DADO: un usuario con ID 1
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("encontrado@test.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // CUANDO: buscamos por ID
        User resultado = userService.findById(1L);

        // ENTONCES: retorna el usuario
        assertEquals(1L, resultado.getId());
        assertEquals("encontrado@test.com", resultado.getEmail());
    }

    @Test
    public void testFindById_UsuarioNoExiste_LanzaExcepcion() {
        // DADO: un ID que no existe
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.findById(999L);
        });
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    // Tests updatePassword() 
    @Test
    public void testUpdatePassword_CambiaYEncripta() {
        // DADO: un usuario existente
        User existente = new User();
        existente.setId(1L);
        existente.setPassword("OLD_HASH");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nuevaClave")).thenReturn("NUEVO_HASH");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // CUANDO: actualizamos password
        User resultado = userService.updatePassword(1L, "nuevaClave");

        // ENTONCES: el password está encriptado con el nuevo valor
        assertEquals("NUEVO_HASH", resultado.getPassword());
        verify(userRepository).save(existente);
    }

    // Tests deleteUser() 
    @Test
    public void testDeleteUser_EliminaCorrectamente() {
        // DADO: un usuario que existe
        when(userRepository.existsById(1L)).thenReturn(true);

        // CUANDO: eliminamos usuario
        userService.deleteUser(1L);

        // ENTONCES: se llama al repositorio
        verify(userRepository).deleteById(1L);
    }

    @Test
    public void testDeleteUser_NoExiste_LanzaExcepcion() {
        // DADO: un ID que no existe
        when(userRepository.existsById(999L)).thenReturn(false);

        // CUANDO/ENTONCES: lanza RuntimeException
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(999L);
        });
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
    }

    // Tests findAllRoles() 
    @Test
    public void testFindAllRoles_RetornaListaDeRoles() {
        // DADO: 3 roles en la BD
        Role admin = new Role();
        admin.setName("ADMIN");
        Role vendedor = new Role();
        vendedor.setName("VENDEDOR");
        Role cliente = new Role();
        cliente.setName("CLIENTE");
        
        when(roleRepository.findAll()).thenReturn(Arrays.asList(admin, vendedor, cliente));

        // CUANDO: obtenemos todos los roles
        List<Role> resultado = userService.findAllRoles();

        // ENTONCES: retorna 3 roles
        assertEquals(3, resultado.size());
    }
}
