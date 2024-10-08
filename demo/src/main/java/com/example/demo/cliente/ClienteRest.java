package com.example.demo.cliente;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

public class ClienteRest {

    private final RestTemplate restTemplate;
    private final String servidorUrl;

    public ClienteRest(String servidorUrl) {
        this.restTemplate = new RestTemplate();
        this.servidorUrl = servidorUrl;
    }

    public void listarTrechos() {
        String url = servidorUrl + "/api/trechos";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        System.out.println("Trechos disponíveis:\n" + response.getBody());
    }

    public void comprarPassagem(String origem, String destino) {
        String url = servidorUrl + "/api/comprar?origem=" + origem + "&destino=" + destino;
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        System.out.println(response.getBody());
    }

    public static void main(String[] args) {
        String servidorUrl = "http://localhost:8083"; // URL do servidor
        ClienteRest cliente = new ClienteRest(servidorUrl);

        cliente.listarTrechos();
        cliente.comprarPassagem("Manaus", "Belem");
        cliente.listarTrechos();
    }
}
