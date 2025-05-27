package com.agendamento.retiradas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.agendamento.retiradas.model.Role;
import com.agendamento.retiradas.model.User;
import com.agendamento.retiradas.repository.UserRepository;

@Configuration
@Profile("prod")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Verificar se já existem usuários no sistema
            if (userRepository.count() == 0) {
                // Criar usuário Vendedor
                User vendedor = new User();
                vendedor.setUsername("vendedor");
                vendedor.setPassword(passwordEncoder.encode("senha123"));
                vendedor.setName("Vendedor Demonstração");
                vendedor.setEmail("vendedor@exemplo.com");
                vendedor.setRole(Role.VENDEDOR);
                vendedor.setEnabled(true);
                userRepository.save(vendedor);
                
                // Criar usuário Expedição
                User expedicao = new User();
                expedicao.setUsername("expedicao");
                expedicao.setPassword(passwordEncoder.encode("senha123"));
                expedicao.setName("Expedição Demonstração");
                expedicao.setEmail("expedicao@exemplo.com");
                expedicao.setRole(Role.EXPEDICAO);
                expedicao.setEnabled(true);
                userRepository.save(expedicao);
                
                System.out.println("Usuários iniciais criados com sucesso!");
            }
        };
    }
}
