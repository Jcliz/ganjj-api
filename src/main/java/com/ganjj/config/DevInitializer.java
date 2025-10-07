package com.ganjj.config;

import com.ganjj.entities.User;
import com.ganjj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadTestData() {
        return args -> {
            if (userRepository.count() == 0) {
                System.out.println("Inicializando usuários de teste...");
                
                User adminUser = new User();
                adminUser.setName("Administrador");
                adminUser.setEmail("admin@ganjj.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole(User.UserRole.ADMIN);
                adminUser.setAddress("Rua Admin, 123");
                userRepository.save(adminUser);
                
                User regularUser = new User();
                regularUser.setName("Cliente Teste");
                regularUser.setEmail("cliente@teste.com");
                regularUser.setPassword(passwordEncoder.encode("cliente123"));
                regularUser.setRole(User.UserRole.USER);
                regularUser.setAddress("Rua Cliente, 456");
                userRepository.save(regularUser);
                
                System.out.println("Usuários de teste criados com sucesso!");
            }
        };
    }
}