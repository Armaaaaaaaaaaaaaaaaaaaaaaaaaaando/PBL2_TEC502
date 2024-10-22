package com.example.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.json.JSONObject;


public class Arquivo {
    
    private static ConcurrentHashMap<String, Map<String, Map<String, Long>>> trechos;

    // Construtor da classe
    public Arquivo() {
        trechos = new ConcurrentHashMap<>(); // Inicializa o mapa de adjacência
    }

    // Método para adicionar cidades e passagens
    public void adicionarCidade(String origem, String destino, Long passagens, String serverId) {
        trechos.putIfAbsent(origem, new HashMap<>());
        Map<String, Long> detalhes = new HashMap<>();
        detalhes.put(serverId, passagens);
        trechos.get(origem).put(destino, detalhes);

        trechos.putIfAbsent(destino, new HashMap<>());
    }



    public static void main(String[] args) {
        //salva para o servidor 1
        String serverId = "1";
        Arquivo adicionador = new Arquivo();
        adicionador.adicionarCidade("Brasilia", "Rio de Janeiro", 10L, serverId);
        adicionador.adicionarCidade("Sao Paulo", "Brasilia", 10L,serverId);
        salvar("cidadesServer1.json");


        serverId = "2";
        adicionador = new Arquivo();
        adicionador.adicionarCidade("Salvador", "Recife", 15L, serverId);
        adicionador.adicionarCidade("Fortaleza", "Belo Horizonte", 20L,serverId);
        salvar("cidadesServer2.json");

        serverId = "3";
        adicionador = new Arquivo();
        adicionador.adicionarCidade("Curitiba", "Porto Alegre", 12L, serverId);
        adicionador.adicionarCidade("Manaus", "Belem", 8L,serverId);
        salvar("cidadesServer3.json");
        
    }



    public static void salvar(String nomedoArquivo){
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
        JSONObject jsonObject = new JSONObject(trechos);

        try (FileWriter file = new FileWriter(arquivoJSON)) {
            file.write(jsonObject.toString());
            file.flush();
            System.out.println("HashMap salvo no arquivo JSON com sucesso em: " + arquivoJSON.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
