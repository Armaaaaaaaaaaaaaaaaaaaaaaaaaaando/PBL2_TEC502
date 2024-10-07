package com.example.tarefas.facade.client;

import java.util.Scanner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
//import org.yaml.snakeyaml.scanner.Scanner;

import com.example.tarefas.dto.MessageDTO; // Certifique-se de que este DTO existe

public class Client {

    private static final String SERVER_URL = "http://localhost:8080/api/mensagens"; // Altere para a URL do seu servidor

    public static void main(String[] args) {
        // Criação do objeto MessageDTO
        MessageDTO message = new MessageDTO();
        message.setSenderId(1);
        while(true){
            Scanner scanner = new Scanner(System.in);
        
            System.out.print("Digite uma mensagem: ");
            String mensagem = scanner.nextLine();
            message.setContent(mensagem);

            // Enviar a mensagem
            sendMessage(message);
            
        }
        
    }


    private static void receiveMessage(ResponseEntity<String> response){
        // Exibir a resposta do servidor
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Response from server: " + response.getBody());
        } else {
            System.out.println("Failed to send message: " + response.getStatusCode());
        }
    }


    private static void sendMessage(MessageDTO message) {
        // Criação de RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Cabeçalhos HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Entidade HTTP (mensagem + cabeçalhos)
        HttpEntity<MessageDTO> requestEntity = new HttpEntity<>(message, headers);

        // Envio da requisição POST
        ResponseEntity<String> response = restTemplate.postForEntity(SERVER_URL, requestEntity, String.class);


        receiveMessage(response);
    }
}
