package com.example.demo.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

// Indica que esta classe contém configurações para o contexto da aplicação Spring
@Configuration
public class AppConfig {

    // Define um bean chamado "restTemplateApp" que estará disponível para injeção na aplicação
    @Bean(name = "restTemplateApp")
    public RestTemplate restTemplateApp() {
        // Cria uma instância de RestTemplate, que é usada para fazer chamadas HTTP
        RestTemplate restTemplate = new RestTemplate();

        // Adiciona um conversor de mensagens para JSON usando Jackson
        // Isso permite que o RestTemplate converta automaticamente JSON para objetos Java e vice-versa
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        // Retorna a instância configurada de RestTemplate
        return restTemplate;
    }
}
