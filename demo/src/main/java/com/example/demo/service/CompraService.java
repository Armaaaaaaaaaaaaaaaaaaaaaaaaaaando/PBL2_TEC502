package com.example.demo.service;

import com.example.demo.model.Trecho;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public String comprar(List<Trecho> rotaEscolhida) {
        solicitarToken(); // Solicita o token para garantir acesso à região crítica
    
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
    
        boolean sucessoNaCompra = true; // Flag para verificar se todos os trechos têm passagens disponíveis
    
        for (Trecho trecho : rotaEscolhida) {
            Trecho trechoLocal = trechos.get(trecho.getOrigem() + "-" + trecho.getDestino());
    
            if (trechoLocal == null || trechoLocal.getPassagensDisponiveis() < 1) {
                // Tenta comprar o trecho em outros servidores se não houver disponibilidade local
                String resposta = tentarComprarEmOutrosServidores(trecho.getOrigem(), trecho.getDestino());
                if (resposta == null || !resposta.contains("Venda feita")) {
                    sucessoNaCompra = false;
                    break;
                }
            } else {
                trechoLocal.setPassagensDisponiveis(trechoLocal.getPassagensDisponiveis() - 1); // Reserva localmente

                
            }
        }
    
        if (sucessoNaCompra) {
            for (Trecho trecho : rotaEscolhida) {
                atualizarTrechosEmTodosOsServidores(trecho);
                if(trechos.containsKey(trecho.getOrigem()+"-"+trecho.getDestino())){
                    Long pssg_de_atualização = trechos.get(trecho.getOrigem()+"-"+trecho.getDestino()).getPassagensDisponiveis(); 
                    trecho.setPassagensDisponiveis(pssg_de_atualização);
                }
            }

            System.out.println(rotaEscolhida);
            //aqui atualiza os trechos
            atualizar_arquivos(rotaEscolhida);

            liberarToken();
            return "Compra realizada com sucesso para a rota: " + rotaEscolhida;
        } else {
            // Desfaz as reservas locais caso a compra falhe
            for (Trecho trecho : rotaEscolhida) {
                Trecho trechoLocal = trechos.get(trecho.getOrigem() + "-" + trecho.getDestino());
                if (trechoLocal != null) {
                    trechoLocal.setPassagensDisponiveis(trechoLocal.getPassagensDisponiveis() + 1);
                }
            }
            liberarToken();
            return "Falha: Um ou mais trechos não têm passagens disponíveis.";
        }
    }
    
    
    
    private String tentarComprarEmOutrosServidores(String origem, String destino) {
        for (String servidor : servidores) {
            // Evita tentar a compra no servidor atual
            if (!servidor.contains(String.valueOf(idServidor))) {
                String url = servidor + "/api/comprarTrecho?origem=" + origem + "&destino=" + destino;
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
        return "Falha: Não foi possível comprar o trecho " + origem + " -> " + destino + " em outros servidores.";
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

    private void atualizarTrechosEmTodosOsServidores(Trecho trecho) {
        for (String servidor : servidores) {
            if (!servidor.contains(String.valueOf(idServidor))) {
                String url = servidor + "/api/atualizarTrecho";
                try {
                    System.out.println("Atualizando trecho no servidor: " + servidor + " com trecho: " + trecho);
                    String response = restTemplate.postForObject(url, trecho, String.class);
                    System.out.println("Resposta do servidor " + servidor + ": " + response);
                } catch (Exception e) {
                    System.err.println("Erro ao atualizar trecho no servidor: " + servidor + " - " + e.getMessage());
                }
            }
        }
    }


    private void iniciarHeartbeats() {
        long intervaloHeartbeat = 5000; 
    
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (String servidor : servidores) {
                    if (!servidor.contains(String.valueOf(idServidor))) {  // Não verifica o próprio servidor
                        try {
                            restTemplate.getForEntity(servidor + "/api/heartbeat", String.class);
                            estadoServidores.put(servidor, true);  // Servidor está ativo
                        } catch (Exception e) {
                            System.err.println("Falha no heartbeat com o servidor: " + servidor);
                            estadoServidores.put(servidor, false);  
    
                            if (tokenHolder.equals(servidor)) {
                                System.out.println("Servidor com o token caiu: " + servidor);
                                regenerarToken();  // Chama o método para regenerar o token
                            }
                        }
                    }
                }
            }
        }, 0, intervaloHeartbeat);
    }
    
    // Método que atualiza arquivos JSON para cada servidor com as rotas correspondentes
    private void atualizar_arquivos(List<Trecho> rotaEscolhida) {
        // Cria uma lista de IDs representando os servidores a serem processados
        List<String> ID_s = new LinkedList<>();
        ID_s.add("1"); // Adiciona o ID do servidor 1
        ID_s.add("2"); // Adiciona o ID do servidor 2
        ID_s.add("3"); // Adiciona o ID do servidor 3

        // Itera sobre cada ID de servidor para organizar e salvar os trechos
        for (String id : ID_s) {
            // Variável que armazenará os trechos organizados para o servidor atual
            ConcurrentHashMap<String, Map<String, Map<String, Long>>> trechos_para_arquivo;

            // Organiza os trechos para o servidor atual com base em seu ID
            trechos_para_arquivo = organizardor_arquivo(id);

            // Salva o resultado no arquivo correspondente ao servidor atual
            salvar_no_arquivo("cidadesServer" + id + ".json", trechos_para_arquivo);
        }
    }

    // Método que organiza os trechos pertencentes a um servidor específico
    public ConcurrentHashMap<String, Map<String, Map<String, Long>>> organizardor_arquivo(String Id_servidor) {
        // Mapa que armazenará os trechos organizados pelo servidor
        ConcurrentHashMap<String, Map<String, Map<String, Long>>> trechos_para_arquivo = new ConcurrentHashMap<>();

        // Itera sobre todos os trechos existentes
        for (Trecho trecho : trechos.values()) {
            // Verifica se o trecho pertence ao servidor especificado
            if (trecho.getServidor().equals(Id_servidor)) {
                // Adiciona a cidade de origem ao mapa, se ainda não existir
                trechos_para_arquivo.putIfAbsent(trecho.getOrigem(), new HashMap<>());

                // Cria um mapa para armazenar detalhes do trecho (servidor e passagens disponíveis)
                Map<String, Long> detalhes = new HashMap<>();
                detalhes.put(trecho.getServidor(), trecho.getPassagensDisponiveis());

                // Adiciona o destino e seus detalhes ao mapa da origem correspondente
                trechos_para_arquivo.get(trecho.getOrigem()).put(trecho.getDestino(), detalhes);

                // Garante que o destino também exista no mapa, mesmo sem trechos disponíveis
                trechos_para_arquivo.putIfAbsent(trecho.getDestino(), new HashMap<>());
            }
        }

        // Retorna o mapa com os trechos organizados para o servidor especificado
        return trechos_para_arquivo;
    }


    public static void salvar_no_arquivo(String nomedoArquivo,ConcurrentHashMap<String, Map<String, Map<String, Long>>> trechos_para_arquivo_1){
        // Caminho e nome do arquivo JSON
        String caminhoPasta = "dados";
        File arquivoJSON = new File(caminhoPasta, nomedoArquivo);

        File pasta = new File(caminhoPasta);
        if (!pasta.exists()) {
            if (pasta.mkdirs()) {
                System.out.println("Pasta criada com sucesso.");
            } else {
                System.out.println("Falha ao criar a pasta.");
                return;
            }
        }

        // Converter HashMap para JSONObject e salvar em arquivo JSON
        JSONObject jsonObject = new JSONObject(trechos_para_arquivo_1);

        try (FileWriter file = new FileWriter(arquivoJSON)) {
            file.write(jsonObject.toString());
            file.flush();
            System.out.println("HashMap salvo no arquivo JSON com sucesso em: " + arquivoJSON.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String comprarTrechoIndividual(String origem, String destino) {
        String chaveTrecho = origem + "-" + destino;
        Trecho trechoLocal = trechos.get(chaveTrecho);
    
        // Verifica a disponibilidade local do trecho usando a chave de origem e destino
        if (trechoLocal != null && trechoLocal.getPassagensDisponiveis() > 0) {
            trechoLocal.setPassagensDisponiveis(trechoLocal.getPassagensDisponiveis() - 1);
            return "Venda feita para o trecho: " + origem + " -> " + destino;
        }
        return "Falha: Sem passagens disponíveis para o trecho " + origem + " -> " + destino;
    }

    private void regenerarToken() {
        synchronized (this) {
            // Encontra o próximo servidor ativo para segurar o token
            for (String servidor : servidores) {
                if (estadoServidores.getOrDefault(servidor, false)) {
                    tokenHolder = servidor;
                    ultimaAtualizacaoToken = System.currentTimeMillis();
                    
                    // Se este servidor for o escolhido para segurar o token
                    if (servidor.equals("http://localhost:" + idServidor)) {
                        System.out.println("Este servidor " + idServidor + " regenerou o token.");
                        notifyAll();  // Notifica os threads esperando pelo token
                    } else {
                        // Passa o token para outro servidor
                        try {
                            String url = servidor + "/api/receberToken";
                            restTemplate.getForEntity(url, String.class);
                            System.out.println("Token regenerado e passado para o servidor: " + servidor);
                        } catch (Exception e) {
                            System.err.println("Erro ao regenerar o token para o servidor: " + servidor + " - " + e.getMessage());
                        }
                    }
                    break;
                }
            }
        }
    }
    
}
