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
    private long tokenTimeout = 10000; // 10 segundos de timeout para o token
    private long ultimaAtualizacaoToken;
    private ConcurrentHashMap<String, Boolean> estadoServidores = new ConcurrentHashMap<>(); // Estado dos servidores
    private Timer heartbeatTimer = new Timer(); //  para os heartbeats


    @Autowired
    public CompraService(RestTemplate restTemplate, ObjectMapper objectMapper, Environment env) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.servidores = Arrays.asList("http://localhost:8081", "http://localhost:8082", "http://localhost:8083");
        this.idServidor = Integer.parseInt(env.getProperty("server.port"));
        this.tokenHolder = servidores.get(0);
        // Inicializa a tarefa de repasse contínuo do token
        iniciarRepasseContinualToken();

        iniciarHeartbeats();

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

        solicitarToken();

        synchronized (this) {
            long tempoInicio = System.currentTimeMillis();
            while (!tokenHolder.equals("http://localhost:" + idServidor)) {
                try {
                    wait(5000); // Timeout de 5 segundos
                    if (System.currentTimeMillis() - tempoInicio > tokenTimeout) {
                        System.err.println("Timeout ao esperar pelo token.");
                        return "Falha: timeout na espera pelo token.";
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "Falha: interrupção na espera.";
                }
            }
        }

        System.out.println("depois de pegar o token= " + conteudo);
        System.out.println("todos os trechos sao assim= " + trechos);

        String resposta = null;

        synchronized (this) {
            System.out.println("Acesso à seção crítica");

            ConcurrentHashMap<String, Trecho> todosOsTrechos = getAllTrechos();
            Trecho trecho = todosOsTrechos.get(conteudo);
            System.out.println("resultado do trecho=" + trecho);

            if (verificarTrechoLocal(trecho)) {
                resposta = "Venda feita: " + origem + " -> " + destino;
            } else {
                resposta = tentarComprarEmOutrosServidores(origem, destino);
            }
        }

        liberarToken();
        return resposta != null ? resposta : "Falha: trecho não encontrado";
    }

    public void solicitarToken() {
        int maxTentativas = 5; // Número de tentativas
        int intervaloEntreTentativas = 2000; // 2 segundos entre cada tentativa
        long tempoInicio = System.currentTimeMillis();
    
        for (int i = 0; i < maxTentativas; i++) {
            synchronized (this) {
                // Se o token já está com este servidor, retorna
                if (tokenHolder.equals("http://localhost:" + idServidor)) {
                    System.out.println("Token já está com o servidor " + idServidor);
                    return; // Já possui o token
                }
    
                // Se não possui o token, aguarda até que o próximo servidor libere o token
                try {
                    System.out.println("Esperando pelo token...");
                    wait(tokenTimeout); // Aguarda o token por um tempo definido
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restaura o estado de interrupção
                    System.err.println("Thread interrompida durante a espera pelo token.");
                    return; // Sai do método em caso de interrupção
                }
    
                // Após o wait, verifica se o token foi recebido
                if (tokenHolder.equals("http://localhost:" + idServidor)) {
                    System.out.println("Token obtido com sucesso pelo servidor " + idServidor);
                    return; // Token recebido
                }
            }
    
            // Espera antes de tentar novamente
            try {
                Thread.sleep(intervaloEntreTentativas);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restaura o estado de interrupção
                System.err.println("Thread interrompida durante o intervalo entre tentativas.");
                break;
            }
    
            // Verifica se o tempo de espera excedeu o limite
            if (System.currentTimeMillis() - tempoInicio > tokenTimeout) {
                System.err.println("Timeout ao solicitar token. Tentativas esgotadas.");
                break;
            }
        }
    
        System.err.println("Falha: não foi possível obter o token após várias tentativas.");
    }
    
    

    public void liberarToken() {
        String proximoServidor = getProximoServidor();
        tokenHolder = proximoServidor;
        ultimaAtualizacaoToken = System.currentTimeMillis();
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
        String proximoServidor = servidores.get(proximoIndice);
    
        // Verifica se o próximo servidor está ativo
        while (!estadoServidores.getOrDefault(proximoServidor, false)) {
            proximoIndice = (proximoIndice + 1) % servidores.size();
            proximoServidor = servidores.get(proximoIndice);
        }
    
        return proximoServidor;
    }
    

    public synchronized void receberToken() {
        tokenHolder = "http://localhost:" + idServidor;
        notifyAll(); // Notifica todos os clientes que estão esperando o token
        ultimaAtualizacaoToken = System.currentTimeMillis();
        System.out.println("Token recebido pelo servidor " + idServidor);
    }

    private void iniciarRepasseContinualToken() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Verifica se o token está parado por muito tempo
                if (System.currentTimeMillis() - ultimaAtualizacaoToken > tokenTimeout) {
                    liberarToken();  // Libera o token automaticamente
                }
            }
        }, tokenTimeout, tokenTimeout);  // Verifica e repassa a cada tokenTimeout
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



    public List<List<Trecho>> montarRota(String origem, String destino) {
        ConcurrentHashMap<String, Trecho> todosOsTrechos = getAllTrechos();
        Map<String, List<Trecho>> grafo = new HashMap<>();
        System.out.println("olha todos os t rechos aqui="+ todosOsTrechos);

        // Constrói o grafo de trechos a partir de todas as origens e destinos 
        for (Trecho trecho : todosOsTrechos.values()) {
            grafo.computeIfAbsent(trecho.getOrigem(), k -> new ArrayList<>()).add(trecho);
        }

        // BFS para encontrar todas as rotas 
        Queue<List<Trecho>> fila = new LinkedList<>();
        List<List<Trecho>> rotas = new ArrayList<>();

        for (Trecho trecho : grafo.getOrDefault(origem, new ArrayList<>())) {
            List<Trecho> caminhoInicial = new ArrayList<>();
            caminhoInicial.add(trecho);
            fila.add(caminhoInicial);
        }

        while (!fila.isEmpty()) {
            List<Trecho> caminhoAtual = fila.poll();
            Trecho ultimoTrecho = caminhoAtual.get(caminhoAtual.size() - 1);
        
            System.out.println("Verificando trecho atual: " + ultimoTrecho);
        
            if (ultimoTrecho.getDestino().equals(destino)) {
                rotas.add(new ArrayList<>(caminhoAtual));
                System.out.println("Rota encontrada: " + caminhoAtual);
            } else {
                for (Trecho proximoTrecho : grafo.getOrDefault(ultimoTrecho.getDestino(), new ArrayList<>())) {
                    List<Trecho> novoCaminho = new ArrayList<>(caminhoAtual);
                    novoCaminho.add(proximoTrecho);
                    fila.add(novoCaminho);
                }
            }
        }
        System.out.println("cheguei aqui no final pra montar a rota.");
        System.out.println("rotas montadas="+rotas);
        return rotas;
    }

    private void iniciarHeartbeats() {
        long intervaloHeartbeat = 5000; // Enviar heartbeat a cada 5 segundos

        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (String servidor : servidores) {
                    if (!servidor.contains(String.valueOf(idServidor))) {
                        verificarHeartbeat(servidor);
                    }
                }
            }
        }, 0, intervaloHeartbeat);
    }

    private void verificarHeartbeat(String servidor) {
        String url = servidor + "/api/heartbeat";
        try {
            restTemplate.getForEntity(url, String.class);
            estadoServidores.put(servidor, true); // Servidor está ativo
            System.out.println("Servidor ativo: " + servidor);
        } catch (Exception e) {
            estadoServidores.put(servidor, false); // Servidor inativo
            System.err.println("Falha no heartbeat do servidor: " + servidor + " - " + e.getMessage());
        }
    }
}
