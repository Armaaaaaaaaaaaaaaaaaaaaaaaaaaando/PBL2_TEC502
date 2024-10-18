package com.example.demo.service;

import com.example.demo.model.Trecho;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class CompraService {

    private ConcurrentHashMap<String, Trecho> trechos = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Variáveis para o algoritmo de Token Ring
    private List<String> servidores;
    private int idServidor;
    private String tokenHolder;

    @Autowired
    public CompraService(RestTemplate restTemplate, ObjectMapper objectMapper, Environment  env) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.servidores = Arrays.asList("http://localhost:8081", "http://localhost:8082", "http://localhost:8083");
        this.idServidor = Integer.parseInt(env.getProperty("server.port")); 
        this.tokenHolder = servidores.get(0);
    }
    

    public ConcurrentHashMap<String, Trecho> getAllTrechos() {
        ConcurrentHashMap<String, Trecho> todosOsTrechos = new ConcurrentHashMap<>(trechos);

        // Adiciona os trechos dos outros servidores
        for (String servidor : servidores) {
            adicionarTrechosDeOutroServidor(todosOsTrechos, servidor + "/api/trecho");
        }

        System.out.println("Todos os trechos: " + todosOsTrechos);
        return todosOsTrechos;
    }

    private void adicionarTrechosDeOutroServidor(ConcurrentHashMap<String, Trecho> todosOsTrechos, String url) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> trechosDeOutroServidor = response.getBody();

            if (trechosDeOutroServidor != null) {
                for (Map.Entry<String, Object> entry : trechosDeOutroServidor.entrySet()) {
                    String chave = entry.getKey();
                    Trecho trecho = objectMapper.convertValue(entry.getValue(), Trecho.class);
                    todosOsTrechos.put(chave, trecho);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao comunicar com o servidor: " + url + " - " + e.getMessage());
        }
    }

    public ConcurrentHashMap<String, Trecho> getAll() {
        return trechos;
    }

    public String comprar(String origem, String destino) {
        String conteudo = origem + "-" + destino;
        System.out.println("antes de solicitar= " + conteudo);
    
        // Solicita permissão para acessar a seção crítica
        solicitarToken();
    
        // Aguarda o token para acessar a seção crítica
        synchronized (this) {
            while (!tokenHolder.equals("http://localhost:" + idServidor)) {
                try {
                    wait(); // Aguarda até receber o token
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "Falha: interrupção na espera.";
                }
            }
        }
    
        System.out.println("depois de pegar o token= " + conteudo);
        System.out.println("todos os trechos sao assim= " + trechos);
    
        String resposta = null;
    
        // Seção crítica
        synchronized (this) {
            System.out.println("Acesso à seção crítica");
    
            ConcurrentHashMap<String, Trecho> todosOsTrechos = getAllTrechos();
            Trecho trecho = todosOsTrechos.get(conteudo);
            System.out.println("resultado do trecho=" + trecho);
    
            if (verificarTrechoLocal(trecho)) {
                resposta = "Venda feita: " + origem + " -> " + destino;
            } else {
                System.out.println("Deu ruim");
                resposta = tentarComprarEmOutrosServidores(origem, destino);
            }
        }
    
        // Libera o token APÓS a compra ter sido confirmada
        liberarToken();
    
        return resposta != null ? resposta : "Falha: trecho não encontrado";
    }
    
    private boolean verificarTrechoLocal(Trecho trecho) {
        if (trecho != null && trecho.getPassagensDisponiveis() > 0) {
            trecho.setPassagensDisponiveis(trecho.getPassagensDisponiveis() - 1);

            // Atualiza os outros servidores
            atualizarTrechosEmTodosOsServidores(trecho);

            return true;
        }
        return false;
    }

    private void atualizarTrechosEmTodosOsServidores(Trecho trecho) {
        for (String servidor : servidores) {
            if (!servidor.contains(String.valueOf(idServidor))) {
                String url = servidor + "/api/atualizarTrecho";
                try {
                    restTemplate.postForObject(url, trecho, String.class);
                    System.out.println("Trecho atualizado no servidor: " + servidor);
                } catch (Exception e) {
                    System.err.println("Erro ao atualizar trecho no servidor: " + servidor + " - " + e.getMessage());
                }
            }
        }
    }

    private String tentarComprarEmOutrosServidores(String origem, String destino) {
        for (String servidor : servidores) {
            if (!servidor.contains(String.valueOf(idServidor))) {
                String url = servidor + "/api/comprar?origem=" + origem + "&destino=" + destino;
                try {
                    String resposta = restTemplate.postForObject(url, null, String.class);
                    if (resposta != null && resposta.contains("Venda feita")) {
                        return resposta;
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao tentar comprar em outro servidor: " + servidor + " - " + e.getMessage());
                }
            }
        }
        return null;
    }

    private void solicitarToken() {
        if (!tokenHolder.equals("http://localhost:" + idServidor)) {
            String proximoServidor = getProximoServidor();
    
            // Solicita o token ao próximo servidor
            String url = proximoServidor + "/api/pedirToken";
            try {
                restTemplate.getForEntity(url, String.class);
                synchronized (this) {
                    wait(5000); // Espera um tempo limitado
                }
            } catch (Exception e) {
                System.err.println("Erro ao solicitar token: " + url + " - " + e.getMessage());
            }
        }
    }
    

    public void liberarToken() {
        String proximoServidor = getProximoServidor();
        tokenHolder = proximoServidor; // Atualiza o holder para o próximo servidor
        String url = proximoServidor + "/api/receberToken";
    
        try {
            restTemplate.getForEntity(url, String.class);
            System.out.println("Token liberado para: " + proximoServidor);
        } catch (Exception e) {
            System.err.println("Erro ao liberar token: " + url + " - " + e.getMessage());
        }
    }
    

    private String getProximoServidor() {
        int indiceAtual = servidores.indexOf("http://localhost:" + idServidor);
        int proximoIndice = (indiceAtual + 1) % servidores.size();
        return servidores.get(proximoIndice);
    }

    public synchronized void receberToken() {
    tokenHolder = "http://localhost:" + idServidor;
    notifyAll(); // Notifica 
    System.out.println("Token recebido pelo servidor " + idServidor);
}

}
