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

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void testSaveUserEncryptsPassword() {
        // Datos
        User user = new User();
        user.setPassword("123456");
        user.setEmail("test@test.com");

        // Mock: Simular que el encoder devuelve un hash
        when(passwordEncoder.encode("123456")).thenReturn("HASH_ENCRIPTADO");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        // Ejecutar
        User savedUser = userService.save(user);

        // Verificar
        assertEquals("HASH_ENCRIPTADO", savedUser.getPassword()); // La clave guardada NO debe ser "123456"
        verify(userRepository).save(user);
    }

    @Test
    public void testFindByEmail() {
        String email = "juan@demo.com";
        User mockUser = new User();
        mockUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(mockUser);

        User found = userService.findByEmail(email);
        assertNotNull(found);
        assertEquals(email, found.getEmail());
    }
}