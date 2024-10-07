package com.example.tarefas.facade.server;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.tarefas.dto.MessageDTO; // Certifique-se de que este DTO existe

@SpringBootApplication
@RestController
@RequestMapping("/api/mensagens")
public class Server {

    public static void main(String[] args) {
        // Inicia a aplicação Spring Boot
        SpringApplication.run(Server.class, args);
        System.out.println("Aguardando respostas!");
    }

    // API REST para receber a mensagem via POST
    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody MessageDTO messageDTO) {
        // Processa a mensagem recebida pela API REST
        System.out.println("mensagem recebida do id: " + messageDTO.getSenderId());
        System.out.println("conteudo da mensagem: " + messageDTO.getContent());

        // Aqui você pode adicionar lógica para processar ou armazenar a mensagem

        // Retorna uma resposta
        return ResponseEntity.ok("a mensagem foi recebida!!");
    }
}
