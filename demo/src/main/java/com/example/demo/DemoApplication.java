package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        startServer("1", 8081);
        startServer("2", 8082);
        startServer("3", 8083);
    }

    private static void startServer(String serverId, int port) {
        new SpringApplicationBuilder(DemoApplication.class)
                .properties("server.port=" + port, "server.id=" + serverId)
                .web(WebApplicationType.SERVLET) // Definimos como aplicativo web
                .run();
    }

    @Bean
    public CommandLineRunner loadData(ApplicationContext context, Environment env) {
        return args -> {
            // Obtém o ID do servidor a partir do ambiente (env)
            String serverId = env.getProperty("server.id");
            AdicionarCidades adicionarCidades = context.getBean(AdicionarCidades.class);

            if (serverId.equals("1")) {
                adicionarCidades.adicionarCidade("Sao Paulo", "Rio de Janeiro", 10L);
                adicionarCidades.adicionarCidade("Sao Paulo", "Brasilia", 10L);
            } else if (serverId.equals("2")) {
                adicionarCidades.adicionarCidade("Salvador", "Recife", 15L);
                adicionarCidades.adicionarCidade("Fortaleza", "Belo Horizonte", 20L);
            } else if (serverId.equals("3")) {
                adicionarCidades.adicionarCidade("Curitiba", "Porto Alegre", 12L);
                adicionarCidades.adicionarCidade("Manaus", "Belem", 8L);
            }
        };
    }
}
