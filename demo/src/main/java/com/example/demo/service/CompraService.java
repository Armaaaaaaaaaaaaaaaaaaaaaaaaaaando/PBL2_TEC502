package com.example.demo.service;

import com.example.demo.model.Trecho;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    public ConcurrentHashMap<String, Trecho> getAll() {
        return trechos;
    }

    public ConcurrentHashMap<String, Trecho> getAllTrechos() {
        // Adiciona os trechos do próprio servidor
        ConcurrentHashMap<String, Trecho> todosOsTrechos = new ConcurrentHashMap<>(trechos);

        // Aguarda que os outros servidores estejam prontos
        //esperarServidoresProntos();

        // Busca os trechos dos outros servidores
        adicionarTrechosDeOutroServidor(todosOsTrechos, "http://localhost:8081/api/trecho");
        adicionarTrechosDeOutroServidor(todosOsTrechos, "http://localhost:8083/api/trecho");

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
            // Tratar erros de comunicação, como servidor indisponível
            System.err.println("Erro ao comunicar com o servidor: " + url + " - " + e.getMessage());
        }
    }

    public String comprar(String origem, String destino){
        //return getAll().toString();
        String conteudo = origem + "-" + destino;
    
        
        for(String chave : trechos.keySet()){
            if(chave.equals(conteudo)){
                trechos.get(chave).setPassagensDisponiveis(trechos.get(chave).getPassagensDisponiveis()-1);
                return "Venda feita";
            }
        }
        return "trecho não encontrado";    

    }
}
