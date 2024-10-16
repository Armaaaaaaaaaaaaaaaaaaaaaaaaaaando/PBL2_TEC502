package com.example.demo.cliente;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClienteRestDistribuido {

    private final RestTemplate restTemplate;
    private final String servidorUrl;

    public ClienteRestDistribuido(String servidorUrl) {
        this.restTemplate = new RestTemplate();
        this.servidorUrl = servidorUrl;
    }

    // Método para comprar passagem
    public void comprarPassagem(String origem, String destino) {
        String url = servidorUrl + "/api/comprar?origem=" + origem + "&destino=" + destino;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            System.out.println("Resultado da compra no servidor " + servidorUrl + ": " + response.getBody());
        } catch (Exception e) {
            System.out.println("Erro ao tentar comprar no servidor " + servidorUrl + ": " + e.getMessage());
        }
    }

    public static void main(String[] args) {
    
        String[] servidores = {
                "http://localhost:8082", 
                "http://localhost:8082", 
                "http://localhost:8082"  
        };

        // Dados pode ser alterado
        String origem = "Sao Paulo";
        String destino = "Rio de Janeiro";

        // Criando um pool de threads para simular clientes em diferentes servidores
        ExecutorService executorService = Executors.newFixedThreadPool(servidores.length);

        for (String servidorUrl : servidores) {
            executorService.submit(() -> {
                ClienteRestDistribuido cliente = new ClienteRestDistribuido(servidorUrl);
                cliente.comprarPassagem(origem, destino);
            });
        }

        executorService.shutdown(); 
    }
}
