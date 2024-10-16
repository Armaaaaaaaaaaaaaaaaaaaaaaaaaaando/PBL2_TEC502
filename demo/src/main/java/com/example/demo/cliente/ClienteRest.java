package com.example.demo.cliente;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClienteRest {

    private final RestTemplate restTemplate;
    private final String servidorUrl;

    public ClienteRest(String servidorUrl) {
        this.restTemplate = new RestTemplate();
        this.servidorUrl = servidorUrl;
    }

    // Método para comprar passagem
    public void comprarPassagem(String origem, String destino) {
        String url = servidorUrl + "/api/comprar?origem=" + origem + "&destino=" + destino;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            System.out.println("Resultado da compra: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Erro ao tentar comprar: " + e.getMessage());
        }
    }

    // Método para realizar múltiplas compras simultâneas
    public void realizarComprasSimultaneas(String origem, String destino, int numeroDeCompras) {
        ExecutorService executorService = Executors.newFixedThreadPool(10); // Cria um pool de 10 threads
        for (int i = 0; i < numeroDeCompras; i++) {
            int finalI = i;
            executorService.submit(() -> {
                System.out.println("Realizando compra #" + (finalI + 1));
                comprarPassagem(origem, destino);
            });
        }
        executorService.shutdown(); // Fecha o pool de threads
    }

    public static void main(String[] args) {
        String servidorUrl = "http://localhost:8082"; // URL do servidor
        ClienteRest cliente = new ClienteRest(servidorUrl);

        // Testar múltiplas compras 
        String origem = "Manaus";
        String destino = "Belem";
        int numeroDeCompras = 1; // Num de compras simultâneas

        cliente.realizarComprasSimultaneas(origem, destino, numeroDeCompras);
    }
}
