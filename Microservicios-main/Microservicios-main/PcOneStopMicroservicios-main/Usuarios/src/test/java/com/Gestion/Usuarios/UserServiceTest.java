package com.Gestion.Usuarios;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.repository.UserRepository;
import com.Gestion.Usuarios.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks private UserService userService;
    @Mock private UserRepository userRepository;

    @Test
    public void testLoginSuccess() {
        String email = "test@demo.com";
        User mockUser = new User(1L, "Test", "User", email, "123456","" ,"CLIENTE");
        when(userRepository.findByEmail(email)).thenReturn(mockUser);

        User found = userService.findByEmail(email);
        assertNotNull(found);
        assertEquals(email, found.getEmail());
    }
}