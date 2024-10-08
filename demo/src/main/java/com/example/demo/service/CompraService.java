package com.example.demo.service;

import com.example.demo.model.Trecho;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class CompraService {
    private ConcurrentHashMap<String, Trecho> trechos = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;

    @Autowired
    public CompraService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public synchronized boolean comprarPassagem(String origem, String destino) {
        Trecho trecho = getTrecho(origem, destino);
        
        if (trecho != null && trecho.getPassagensDisponiveis() > 0) {
            // Compra localmente
            trecho.setPassagensDisponiveis(trecho.getPassagensDisponiveis() - 1);
            return true;
        }
        
        return verificarEmOutroServidor(origem, destino);
    }

    private boolean verificarEmOutroServidor(String origem, String destino) {
        String[] outrosServidores = {
            "http://localhost:8082",
            "http://localhost:8083"
        };
    
        for (String servidor : outrosServidores) {
            try {
                // Verifica se o trecho existe e tem passagens disponíveis
                String urlTrecho = servidor + "/api/trecho?origem=" + origem + "&destino=" + destino;
                Trecho trechoRemoto = restTemplate.getForObject(urlTrecho, Trecho.class);
    
                if (trechoRemoto != null && trechoRemoto.getPassagensDisponiveis() > 0) {
                    // Tenta comprar a passagem no outro servidor
                    boolean compraRealizada = comprarTrechoEmOutroServidor(servidor, origem, destino);
                    if (compraRealizada) {
                        // Atualiza o servidor remoto após a compra
                        atualizarTrechoNoServidor(servidor, origem, destino);
                        return true;
                    }
                }
            } catch (Exception e) {
                System.out.println("Erro ao verificar trecho em outro servidor: " + servidor + " - " + e.getMessage());
            }
        }
        return false;
    }
    
    
    
    

    private boolean comprarTrechoEmOutroServidor(String servidor, String origem, String destino) {
        try {
            String urlCompra = servidor + "/api/comprar?origem=" + origem + "&destino=" + destino;
            String response = restTemplate.postForObject(urlCompra, null, String.class);
    
            if (response != null && response.contains("Passagem comprada com sucesso!")) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Erro ao tentar comprar passagem em outro servidor: " + servidor + " - " + e.getMessage());
        }
        return false;
    }
    
    
    

    private void atualizarTrechoNoServidor(String servidorUrl, String origem, String destino) {
        try {
            Trecho trecho = getTrecho(origem, destino);
            if (trecho != null) {
                // Cria o corpo com o número de passagens atualizadas
                String requestBody = "{ \"origem\": \"" + origem + "\", \"destino\": \"" + destino + "\", \"passagensDisponiveis\": " + trecho.getPassagensDisponiveis() + " }";
    
                // Define o cabeçalho como JSON
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Type", "application/json");
    
                // Envia a requisição PUT com o trecho atualizado
                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
                restTemplate.exchange(servidorUrl + "/api/atualizarTrecho", HttpMethod.PUT, entity, String.class);
            }
        } catch (Exception e) {
            System.out.println("Erro ao atualizar trecho no servidor: " + servidorUrl + " - " + e.getMessage());
        }
    }
    
    

    public Trecho getTrecho(String origem, String destino) {
        return trechos.get(origem + "-" + destino);
    }

    public ConcurrentHashMap<String, Trecho> getAllTrechos() {
        return trechos;
    }
}
