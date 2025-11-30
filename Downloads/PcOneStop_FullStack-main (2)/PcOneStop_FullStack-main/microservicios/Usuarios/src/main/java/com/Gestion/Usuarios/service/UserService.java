package com.Gestion.Usuarios.service;

import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.repository.UserRepository;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User save(User user) {
        // PRIMERO: Validar rol (antes de usarlo para generar ID)
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("CLIENT");
        }
        // SEGUNDO: Generar ID si no viene
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId("user-" + user.getRole().toLowerCase() + "-" + UUID.randomUUID().toString().substring(0, 6));
        }
        // TERCERO: Encriptar contraseña
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }

    public User updatePassword(String id, String newPass) {
        User u = findById(id);
        u.setPassword(passwordEncoder.encode(newPass));
        return userRepository.save(u);
    }

    public User update(String id, User userData) {
        User existingUser = findById(id);
        
        existingUser.setName(userData.getName());
        existingUser.setLastName(userData.getLastName());
        existingUser.setEmail(userData.getEmail());
        existingUser.setRole(userData.getRole());
        
        // Solo actualizar password si viene en el request
        if (userData.getPassword() != null && !userData.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userData.getPassword()));
        }
        
        return userRepository.save(existingUser);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }
}
