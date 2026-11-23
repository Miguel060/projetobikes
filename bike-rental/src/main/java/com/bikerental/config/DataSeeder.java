package com.bikerental.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bikerental.model.*;
import com.bikerental.repository.*;

/** Dados iniciais para demonstração (só roda com o banco vazio). */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UsuarioRepository usuarios, ClienteRepository clientes,
                           BicicletaRepository bicicletas, PasswordEncoder encoder) {
        return args -> {
            if (usuarios.count() > 0) {
                return;
            }
            usuarios.save(novoUsuario("admin", "admin123", Role.ADMIN, encoder));
            Usuario usuarioCliente = usuarios.save(novoUsuario("cliente", "cliente123", Role.CLIENTE, encoder));

            Cliente cliente = new Cliente();
            cliente.setNome("Cliente Demonstração");
            cliente.setCpf("12345678901");
            cliente.setTelefone("(49) 99999-0000");
            cliente.setEmail("cliente@exemplo.com");
            cliente.setUsuario(usuarioCliente);
            clientes.save(cliente);

            bicicletas.save(novaBicicleta("BIKE-001", "Caloi Urbana"));
            bicicletas.save(novaBicicleta("BIKE-002", "Sense Montanha"));
            bicicletas.save(novaBicicleta("BIKE-003", "Oggi Dobrável"));
        };
    }

    private Usuario novoUsuario(String username, String senha, Role role, PasswordEncoder encoder) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(senha));
        u.setRole(role);
        return u;
    }

    private Bicicleta novaBicicleta(String codigo, String modelo) {
        Bicicleta b = new Bicicleta();
        b.setCodigo(codigo);
        b.setModelo(modelo);
        return b;
    }
}
