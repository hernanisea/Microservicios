package com.Resenas.Calificaciones.config;

import com.Resenas.Calificaciones.model.Review;
import com.Resenas.Calificaciones.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * DataLoader para precargar datos iniciales en la base de datos de Calificaciones.
 * Se ejecuta automáticamente al iniciar la aplicación.
 * 
 * Datos precargados:
 * - Reseñas de ejemplo para varios productos
 * 
 * IDs de referencia:
 * - Clientes: Juan (4), Ana (5), Luis (6)
 * - Productos: IDs del 1 al 14 (ver DataLoader de Inventario)
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public void run(String... args) throws Exception {
        if (reviewRepository.count() == 0) {
            loadReviews();
        }
        
        System.out.println("[Calificaciones] Datos precargados correctamente");
        System.out.println("   Reseñas: " + reviewRepository.count());
    }

    private void loadReviews() {
        System.out.println("Cargando reseñas de prueba...");

        // ===== RESEÑAS PARA RTX 4070 Super (Producto 1) =====
        Review r1 = new Review();
        r1.setProductId(1L);
        r1.setUserId(4L); // Juan
        r1.setRating(5);
        r1.setComment("Excelente tarjeta! Corre todo en ultra a 1440p. El DLSS 3 es increíble.");
        r1.setDate(LocalDate.now().minusDays(10));
        reviewRepository.save(r1);

        Review r2 = new Review();
        r2.setProductId(1L);
        r2.setUserId(5L); // Ana
        r2.setRating(4);
        r2.setComment("Muy buena GPU, aunque esperaba mejor rendimiento en ray tracing. Aún así la recomiendo.");
        r2.setDate(LocalDate.now().minusDays(5));
        reviewRepository.save(r2);

        // ===== RESEÑAS PARA RX 7800 XT (Producto 2) =====
        Review r3 = new Review();
        r3.setProductId(2L);
        r3.setUserId(6L); // Luis
        r3.setRating(5);
        r3.setComment("Team Red! Gran rendimiento por el precio. 16GB de VRAM es perfecto para el futuro.");
        r3.setDate(LocalDate.now().minusDays(20));
        reviewRepository.save(r3);

        // ===== RESEÑAS PARA Ryzen 7 7800X3D (Producto 4) =====
        Review r4 = new Review();
        r4.setProductId(4L);
        r4.setUserId(4L); // Juan
        r4.setRating(5);
        r4.setComment("El mejor CPU gaming sin discusión. La V-Cache hace magia en los juegos.");
        r4.setDate(LocalDate.now().minusDays(8));
        reviewRepository.save(r4);

        Review r5 = new Review();
        r5.setProductId(4L);
        r5.setUserId(6L); // Luis
        r5.setRating(5);
        r5.setComment("Temperaturas bajas y rendimiento brutal. Vale cada sol.");
        r5.setDate(LocalDate.now().minusDays(3));
        reviewRepository.save(r5);

        // ===== RESEÑAS PARA Intel i7-13700K (Producto 5) =====
        Review r6 = new Review();
        r6.setProductId(5L);
        r6.setUserId(5L); // Ana
        r6.setRating(4);
        r6.setComment("Buen procesador para productividad. Algo caliente pero nada que un buen cooler no solucione.");
        r6.setDate(LocalDate.now().minusDays(25));
        reviewRepository.save(r6);

        // ===== RESEÑAS PARA RAM Corsair DDR5 (Producto 7) =====
        Review r7 = new Review();
        r7.setProductId(7L);
        r7.setUserId(4L); // Juan
        r7.setRating(4);
        r7.setComment("RAM sólida, el RGB se ve genial con iCUE. XMP funcionó a la primera.");
        r7.setDate(LocalDate.now().minusDays(12));
        reviewRepository.save(r7);

        // ===== RESEÑAS PARA Samsung 990 PRO (Producto 9) =====
        Review r8 = new Review();
        r8.setProductId(9L);
        r8.setUserId(6L); // Luis
        r8.setRating(5);
        r8.setComment("Velocidad absurda. Los juegos cargan en segundos. El mejor SSD que he tenido.");
        r8.setDate(LocalDate.now().minusDays(7));
        reviewRepository.save(r8);

        Review r9 = new Review();
        r9.setProductId(9L);
        r9.setUserId(5L); // Ana
        r9.setRating(5);
        r9.setComment("Perfecto para edición de video. Transfiere archivos 4K como si nada.");
        r9.setDate(LocalDate.now().minusDays(2));
        reviewRepository.save(r9);

        // ===== RESEÑAS PARA Logitech G Pro X (Producto 13) =====
        Review r10 = new Review();
        r10.setProductId(13L);
        r10.setUserId(4L); // Juan
        r10.setRating(5);
        r10.setComment("Teclado premium. Los switches se sienten increíbles y poder cambiarlos es genial.");
        r10.setDate(LocalDate.now().minusDays(14));
        reviewRepository.save(r10);

        // ===== RESEÑAS PARA Razer DeathAdder (Producto 14) =====
        Review r11 = new Review();
        r11.setProductId(14L);
        r11.setUserId(5L); // Ana
        r11.setRating(5);
        r11.setComment("El mejor mouse que he usado. Súper ligero y la batería dura semanas.");
        r11.setDate(LocalDate.now().minusDays(28));
        reviewRepository.save(r11);

        Review r12 = new Review();
        r12.setProductId(14L);
        r12.setUserId(6L); // Luis
        r12.setRating(4);
        r12.setComment("Muy cómodo para sesiones largas. El sensor es preciso. Solo le falta más RGB.");
        r12.setDate(LocalDate.now().minusDays(18));
        reviewRepository.save(r12);

        // ===== RESEÑA ADICIONAL CON RATING BAJO (para diversidad) =====
        Review r13 = new Review();
        r13.setProductId(3L); // RTX 4060 Ti
        r13.setUserId(6L); // Luis
        r13.setRating(3);
        r13.setComment("Esperaba más por el precio. Para 1080p está bien pero el 4070 hubiera sido mejor inversión.");
        r13.setDate(LocalDate.now().minusDays(22));
        reviewRepository.save(r13);

        System.out.println("13 reseñas creadas para 9 productos diferentes:");
        System.out.println("   (5 estrellas): 9 reseñas");
        System.out.println("   (4 estrellas): 3 reseñas");
        System.out.println("   (3 estrellas): 1 reseña");
    }
}




