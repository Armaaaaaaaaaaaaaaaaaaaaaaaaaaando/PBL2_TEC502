package com.example.demo.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// Indica que esta classe fornece configurações para o contexto da aplicação Spring
@Configuration
public class RestTemplateConfig {

    // Define um bean de RestTemplate, que pode ser injetado em outras classes da aplicação
    @Bean
    public RestTemplate restTemplate() {
        // Cria e retorna uma nova instância de RestTemplate
        // O RestTemplate é usado para fazer chamadas HTTP e facilita a comunicação com APIs externas
        return new RestTemplate();
    }
}
