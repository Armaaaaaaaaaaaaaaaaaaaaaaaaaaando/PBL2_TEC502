package com.example.demo.cliente;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
    //retirar talvez...
    public void comprarPassagem(String origem, String destino) {
        String url = servidorUrl + "/api/comprar?origem=" + origem + "&destino=" + destino;
        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        System.out.println(response.getBody());
    }

    public static void main(String[] args) {
        String servidorUrl = "http://localhost:8082"; // URL do servidor
        ClienteRest cliente = new ClienteRest(servidorUrl);
        System.out.println("Antes da compra");
        cliente.listarTrechos();
        cliente.comprar("Salvador", "Recife");

        System.out.println("Depois da compra");
        cliente.listarTrechos();
    }


    public void comprar(String origem, String destino){
        String url = servidorUrl+"/api/comprar";

        MultiValueMap<String,String> parametros = new LinkedMultiValueMap<>();
        parametros.add("origem", origem);
        parametros.add("destino",destino);


        ResponseEntity<String> response = restTemplate.postForEntity(url, parametros, String.class);
        System.out.println(response.getBody());
    }
}
