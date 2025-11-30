package com.Gestion.Usuarios.repository;

import com.Gestion.Usuarios.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    List<User> findByRole(String role);
}
