package com.Gestion.Usuarios.service;



import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.repository.UserRepository;
import jakarta.transaction.Transactional;

import java.util.List;

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
        // Aquí podrías encriptar la contraseña antes de guardar
       user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
    
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    public User updatePassword(Long id, String newPass) {
        User u = findById(id);
        u.setPassword(passwordEncoder.encode(newPass)); // Encriptar aquí también
        return userRepository.save(u);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}