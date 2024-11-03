package com.example.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ch.qos.logback.core.FileAppender;
import java.io.FileReader;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



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



    public static void main(String[] args) throws org.apache.tomcat.util.json.ParseException, ParseException, IOException {
       

        
        //salva para o servidor 1
        String serverId = "1";
        Arquivo adicionador = new Arquivo();
        adicionador.adicionarCidade("Brasilia", "Rio de Janeiro", 10L, serverId);
        adicionador.adicionarCidade("Sao Paulo", "Brasilia", 10L,serverId);
        salvar("cidadesServer1.json");


        serverId = "2";
        adicionador = new Arquivo();
        adicionador.adicionarCidade("Salvador", "Rio de Janeiro", 15L, serverId);
        adicionador.adicionarCidade("Sao Paulo", "Salvador", 20L,serverId);
        salvar("cidadesServer2.json");

        serverId = "3";
        adicionador = new Arquivo();
        adicionador.adicionarCidade("Curitiba", "Porto Alegre", 12L, serverId);
        adicionador.adicionarCidade("Manaus", "Belem", 8L,serverId);
        salvar("cidadesServer3.json");
        
         // Caminho e nome do arquivo JSON
         String caminhoPasta = "demo/dados";
         String nomeArquivo = "cidadesServer1.json";
         File arquivoJSON = new File(caminhoPasta, nomeArquivo);
         ler_cidades(arquivoJSON);


    }



    public static void salvar(String nomedoArquivo){
        // Caminho e nome do arquivo JSON
        String caminhoPasta = "demo/dados";
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
    

    public static void ler_cidades(File arquivoJSON) throws ParseException, IOException {
        JSONParser parser = new JSONParser();  // Sem argumentos no construtor
        ConcurrentHashMap<String, Map<String, Map<String, Long>>> vindo_arquivo;
        
         
        try (FileReader leitor = new FileReader(arquivoJSON)) {
            JSONObject jsonObject = (JSONObject) parser.parse(leitor);
            vindo_arquivo = new ConcurrentHashMap<>();

            
            vindo_arquivo.putAll(jsonObject);

            System.out.println(vindo_arquivo);



        }
        catch (IOException | ParseException e) {
                e.printStackTrace();
            }
    }


    



    
}
