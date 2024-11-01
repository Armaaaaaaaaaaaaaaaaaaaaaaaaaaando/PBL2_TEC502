package com.example.demo.cliente;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;
import com.example.demo.model.Trecho;

public class CompraPassagem {
    

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "http://localhost:8083/api/comprar";

        // Cria a lista de trechos para a compra
        List<Trecho> rotaEscolhida = Arrays.asList(
            new Trecho("Sao Paulo", "Salvador", 20, "2"),
            new Trecho("Salvador", "Rio de Janeiro", 15, "2")
        );

        // requisição POST
        ResponseEntity<String> response = restTemplate.postForEntity(url, rotaEscolhida, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Compra realizada com sucesso: " + response.getBody());
        } else {
            System.out.println("Falha na compra: " + response.getStatusCode());
        }
    }
}
