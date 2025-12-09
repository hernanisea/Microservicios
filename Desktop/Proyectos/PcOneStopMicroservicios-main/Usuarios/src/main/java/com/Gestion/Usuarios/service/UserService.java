package com.Gestion.Usuarios.service;

import com.Gestion.Usuarios.model.Role;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.repository.RoleRepository;
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
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo usuario en el sistema.
     * Valida email único, encripta la contraseña y asigna el rol correspondiente.
     */
    public User save(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String roleName = user.getRoleName();
        if (roleName == null || roleName.isBlank()) {
            roleName = "CLIENTE";
        }

        final String finalRoleName = roleName.toUpperCase();
        Role role = roleRepository.findByName(finalRoleName)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + finalRoleName));

        user.setRole(role);
        return userRepository.save(user);
    }

    /**
     * Busca un usuario por su correo electrónico.
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Obtiene todos los usuarios registrados.
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Busca un usuario por su ID.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Actualiza la contraseña de un usuario.
     */
    public User updatePassword(Long id, String newPass) {
        User u = findById(id);
        u.setPassword(passwordEncoder.encode(newPass));
        return userRepository.save(u);
    }

    /**
     * Elimina un usuario del sistema.
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Obtiene todos los roles disponibles.
     */
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }
}
