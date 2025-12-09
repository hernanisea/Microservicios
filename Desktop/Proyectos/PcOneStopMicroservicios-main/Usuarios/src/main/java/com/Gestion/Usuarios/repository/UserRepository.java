package com.Gestion.Usuarios.repository;

import com.Gestion.Usuarios.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Busca un usuario por su correo electrónico.
     */
    User findByEmail(String email);
    
    /**
     * Verifica si un correo electrónico ya está registrado.
     */
    boolean existsByEmail(String email);
}