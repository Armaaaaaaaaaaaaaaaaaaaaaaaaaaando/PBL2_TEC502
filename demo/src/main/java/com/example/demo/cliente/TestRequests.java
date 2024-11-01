package com.example.demo.cliente;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestRequests {

    private static final String URL_A = "http://localhost:8081/api/comprar";
    private static final String URL_B = "http://localhost:8082/api/comprar";
    private static final String URL_C = "http://localhost:8083/api/comprar";
    private static final String TRECHOS_URL_A = "http://localhost:8081/api/trechos";
    private static final String TRECHOS_URL_B = "http://localhost:8082/api/trechos";

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Criar o payload JSON para a compra
        String jsonPayload = "[{\"origem\": \"Sao Paulo\", \"destino\": \"Rio de Janeiro\"}]";

        // Enviar requisições em paralelo
        executor.submit(() -> sendPostRequest(client, URL_A, jsonPayload));
        executor.submit(() -> sendPostRequest(client, URL_B, jsonPayload));
        executor.submit(() -> sendPostRequest(client, URL_C, jsonPayload));

        // Aguardar todas as requisições
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Aguarde a finalização das tarefas
        }

        // Verificar disponibilidade de trechos após as tentativas de compra
        sendGetRequest(client, TRECHOS_URL_A);
        sendGetRequest(client, TRECHOS_URL_B);
    }

    private static void sendPostRequest(HttpClient client, String url, String json) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response from " + url + ": " + response.body());
        } catch (Exception e) {
            System.out.println("Error occurred for " + url + ": " + e.getMessage());
        }
    }

    private static void sendGetRequest(HttpClient client, String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Trechos de " + url + ": " + response.body());
        } catch (Exception e) {
            System.out.println("Error occurred for " + url + ": " + e.getMessage());
        }
    }
}
