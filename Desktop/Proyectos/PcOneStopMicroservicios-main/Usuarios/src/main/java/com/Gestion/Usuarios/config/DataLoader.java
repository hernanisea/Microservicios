package com.Gestion.Usuarios.config;

import com.Gestion.Usuarios.model.Role;
import com.Gestion.Usuarios.model.User;
import com.Gestion.Usuarios.repository.RoleRepository;
import com.Gestion.Usuarios.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataLoader para precargar datos iniciales en la base de datos de Usuarios.
 * Se ejecuta automáticamente al iniciar la aplicación.
 * 
 * Datos precargados:
 * - 3 Roles: ADMIN, VENDEDOR, CLIENTE
 * - 1 Admin, 2 Vendedores, 3 Clientes de prueba
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Solo cargar si no hay datos
        if (roleRepository.count() == 0) {
            loadRoles();
        }
        
        if (userRepository.count() == 0) {
            loadUsers();
        }
        
        System.out.println("[Usuarios] Datos precargados correctamente");
        System.out.println("   Roles: " + roleRepository.count());
        System.out.println("   Usuarios: " + userRepository.count());
    }

    private void loadRoles() {
        System.out.println("Cargando roles...");
        
        Role admin = new Role();
        admin.setName("ADMIN");
        admin.setDescription("Administrador del sistema con acceso total");
        roleRepository.save(admin);

        Role vendedor = new Role();
        vendedor.setName("VENDEDOR");
        vendedor.setDescription("Vendedor que puede publicar y gestionar productos");
        roleRepository.save(vendedor);

        Role cliente = new Role();
        cliente.setName("CLIENTE");
        cliente.setDescription("Cliente que puede comprar productos y dejar reseñas");
        roleRepository.save(cliente);
        
        System.out.println("3 roles creados: ADMIN, VENDEDOR, CLIENTE");
    }

    private void loadUsers() {
        System.out.println("Cargando usuarios de prueba...");
        
        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        Role vendedorRole = roleRepository.findByName("VENDEDOR").orElseThrow();
        Role clienteRole = roleRepository.findByName("CLIENTE").orElseThrow();

        // ===== ADMINISTRADOR =====
        User admin = new User();
        admin.setFirstName("Carlos");
        admin.setLastName("Administrador");
        admin.setEmail("admin@pconestop.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setPhone("+51 999000001");
        admin.setRole(adminRole);
        userRepository.save(admin);

        // ===== VENDEDORES =====
        User vendedor1 = new User();
        vendedor1.setFirstName("María");
        vendedor1.setLastName("TechStore");
        vendedor1.setEmail("maria@techstore.com");
        vendedor1.setPassword(passwordEncoder.encode("vendedor123"));
        vendedor1.setPhone("+51 999000002");
        vendedor1.setRole(vendedorRole);
        userRepository.save(vendedor1);

        User vendedor2 = new User();
        vendedor2.setFirstName("Pedro");
        vendedor2.setLastName("PCMaster");
        vendedor2.setEmail("pedro@pcmaster.com");
        vendedor2.setPassword(passwordEncoder.encode("vendedor123"));
        vendedor2.setPhone("+51 999000003");
        vendedor2.setRole(vendedorRole);
        userRepository.save(vendedor2);

        // ===== CLIENTES =====
        User cliente1 = new User();
        cliente1.setFirstName("Juan");
        cliente1.setLastName("Pérez");
        cliente1.setEmail("juan@gmail.com");
        cliente1.setPassword(passwordEncoder.encode("cliente123"));
        cliente1.setPhone("+51 987654321");
        cliente1.setRole(clienteRole);
        userRepository.save(cliente1);

        User cliente2 = new User();
        cliente2.setFirstName("Ana");
        cliente2.setLastName("García");
        cliente2.setEmail("ana@gmail.com");
        cliente2.setPassword(passwordEncoder.encode("cliente123"));
        cliente2.setPhone("+51 987654322");
        cliente2.setRole(clienteRole);
        userRepository.save(cliente2);

        User cliente3 = new User();
        cliente3.setFirstName("Luis");
        cliente3.setLastName("Rodríguez");
        cliente3.setEmail("luis@gmail.com");
        cliente3.setPassword(passwordEncoder.encode("cliente123"));
        cliente3.setPhone("+51 987654323");
        cliente3.setRole(clienteRole);
        userRepository.save(cliente3);

        System.out.println("6 usuarios creados: 1 admin, 2 vendedores, 3 clientes");
        System.out.println("Credenciales de prueba:");
        System.out.println("   Admin:    admin@pconestop.com / admin123");
        System.out.println("   Vendedor: maria@techstore.com / vendedor123");
        System.out.println("   Vendedor: pedro@pcmaster.com / vendedor123");
        System.out.println("   Cliente:  juan@gmail.com / cliente123");
    }
}




