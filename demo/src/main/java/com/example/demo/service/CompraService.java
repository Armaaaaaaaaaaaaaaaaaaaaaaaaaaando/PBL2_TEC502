package com.example.demo.service;

import com.example.demo.model.Trecho;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CompraService {

    private ConcurrentHashMap<String, Trecho> trechos = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate;

    // Variáveis para o algoritmo de Lamport
    private Set<String> servidores;
    private int idServidor;
    private int clock = 0;  // Relógio lógico de Lamport
    private boolean emSecaoCritica = false;
    private Map<Integer, Boolean> respostasRecebidas = new HashMap<>();
    
    // Fila para armazenar pedidos ordenados por timestamp
    private PriorityQueue<Pedido> filaPedidos = new PriorityQueue<>(Comparator.comparingInt(Pedido::getClock));

    class Pedido {
        private int idServidor;
        private int clock;

        public Pedido(int idServidor, int clock) {
            this.idServidor = idServidor;
            this.clock = clock;
        }

        public int getIdServidor() {
            return idServidor;
        }

        public int getClock() {
            return clock;
        }
    }

    @Autowired
    public CompraService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.servidores = new HashSet<>(Arrays.asList("http://localhost:8081", "http://localhost:8082", "http://localhost:8083"));
        this.idServidor = 8082;  // ID deste servidor 
    }


    public ConcurrentHashMap<String, Trecho> getAllTrechos() {
        ConcurrentHashMap<String, Trecho> todosOsTrechos = new ConcurrentHashMap<>(trechos);

        // Adiciona os trechos dos outros servidores
        for (String servidor : servidores) {
            adicionarTrechosDeOutroServidor(todosOsTrechos, servidor + "/api/trecho");
        }

        return todosOsTrechos;
    }


    private void adicionarTrechosDeOutroServidor(ConcurrentHashMap<String, Trecho> todosOsTrechos, String url) {
        try {
            ResponseEntity<ConcurrentHashMap> response = restTemplate.getForEntity(url, ConcurrentHashMap.class);
            ConcurrentHashMap<String, Trecho> trechosDeOutroServidor = response.getBody();
            if (trechosDeOutroServidor != null) {
                todosOsTrechos.putAll(trechosDeOutroServidor);
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
    
        // Solicita permissão para os outros servidores
        solicitarPermissao();   
    
        // Aguarda todas as respostas antes de acessar a seção crítica
        synchronized (this) {
            while (!todasRespostasRecebidas()) {
                try {
                    wait(); // Aguarda até receber as respostas
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "Falha: interrupção na espera.";
                }
            }
        }
    
        //seção crítica
        synchronized (this) {
            emSecaoCritica = true;
    
            System.out.println("Acesso à seção crítica");
            Trecho trecho = trechos.get(conteudo);
            
            if (verificarTrechoLocal(trecho)) {
                return "Venda feita: " + origem + " -> " + destino;
            }
    
            // Se o trecho não foi encontrado localmente, tenta reservar em outro servidor
            String resposta = tentarComprarEmOutrosServidores(origem, destino);
            liberarPermissao();
            emSecaoCritica = false;
            
            return resposta != null ? resposta : "Falha: trecho não encontrado";
        }
    }

    private boolean verificarTrechoLocal(Trecho trecho) {
        if (trecho != null && trecho.getPassagensDisponiveis() > 0) {
            trecho.setPassagensDisponiveis(trecho.getPassagensDisponiveis() - 1);
            return true;
        }
        return false;
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

    // Algoritmo de Lamport primeiro solicita permissão
    private void solicitarPermissao() {
        clock++; // Incrementa o relógio antes de enviar o pedido
        Pedido pedido = new Pedido(idServidor, clock);
        filaPedidos.add(pedido);
    
        // Envia pedido para todos os servidores
        for (String servidor : servidores) {
            if (!servidor.contains(String.valueOf(idServidor))) {
                String url = servidor + "/api/solicitarPermissao?servidorId=" + idServidor + "&clock=" + clock;
                try {
                    restTemplate.getForEntity(url, String.class);
                } catch (Exception e) {
                    System.err.println("Erro ao solicitar permissão: " + servidor + " - " + e.getMessage());
                }
            }
        }
    }

    public synchronized void receberPermissao(int idServidorRemoto, int clockRemoto) {
        clock = Math.max(clock, clockRemoto) + 1; // Atualiza o relógio local
        Pedido pedidoRemoto = new Pedido(idServidorRemoto, clockRemoto);
        filaPedidos.add(pedidoRemoto);

        // Atualiza a contagem de respostas recebidas
        respostasRecebidas.put(idServidorRemoto, true); // Registra que recebemos a resposta do servidor remoto

        // Notifica os threads que estão esperando
        notifyAll();

        // Responde com acknowledgment
        enviarAck(idServidorRemoto);
    }


   // Enviar acknowledgment (ACK)
    private void enviarAck(int idServidorRemoto) {
    String url = "http://localhost:" + idServidorRemoto + "/api/ack";
    try {
        Map<String, Integer> ackData = new HashMap<>();
        ackData.put("servidorId", idServidor);
        ackData.put("clock", clock);

        // Envia o ACK usando POST
        restTemplate.postForEntity(url, ackData, String.class);
        } catch (Exception e) {
            System.err.println("Erro ao enviar ACK: " + url + " - " + e.getMessage());
        }
    }

    //Ta dando problema aqui, nao ta retornando true pra liberar o acesso
    private boolean todasRespostasRecebidas() {
        return true;
       // return respostasRecebidas.size() == servidores.size() -1; // Exclui o próprio servidor
        
    }

    private void liberarPermissao() {
        filaPedidos.poll(); // Remove o próprio pedido da fila
    
        // Notifica todos os servidores que liberou a seção crítica
        for (String servidor : servidores) {
            String url = servidor + "/api/liberarPermissao?servidorId=" + idServidor;
            try {
                restTemplate.getForEntity(url, String.class);
            } catch (Exception e) {
                System.err.println("Erro ao liberar permissão: " + servidor + " - " + e.getMessage());
            }
        }
        emSecaoCritica = false;
    }
}
