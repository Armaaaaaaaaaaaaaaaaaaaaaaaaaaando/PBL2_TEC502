package com.example.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Arquivo {
    
    // Mapa concorrente para armazenar trechos entre cidades, onde cada entrada contém a origem,
    // destinos e detalhes das passagens (quantidade e servidor)
    private static ConcurrentHashMap<String, Map<String, Map<String, Long>>> trechos;

    // Construtor da classe: inicializa o mapa de trechos
    public Arquivo() {
        trechos = new ConcurrentHashMap<>(); // Inicializa o mapa de adjacência
    }

    // Método para adicionar uma cidade ao mapa de trechos
    public void adicionarCidade(String origem, String destino, Long passagens, String serverId) {
        // Verifica se a origem já existe, se não, a adiciona
        trechos.putIfAbsent(origem, new HashMap<>());

        // Cria um mapa de detalhes com servidor e passagens disponíveis
        Map<String, Long> detalhes = new HashMap<>();
        detalhes.put(serverId, passagens);

        // Adiciona o destino e detalhes de passagem ao mapa de origem
        trechos.get(origem).put(destino, detalhes);

        // Garante que o destino também esteja no mapa (sem conexão inicial)
        trechos.putIfAbsent(destino, new HashMap<>());
    }

    public static void main(String[] args) throws ParseException, IOException {
        // Exemplo de como salvar dados de trechos para diferentes servidores

        // Servidor 1: Adiciona cidades e salva em um arquivo JSON
        String serverId = "1";
        Arquivo adicionador = new Arquivo();
        adicionador.adicionarCidade("Brasilia", "Rio de Janeiro", 10L, serverId);
        adicionador.adicionarCidade("Sao Paulo", "Brasilia", 10L, serverId);
        salvar("cidadesServer1.json");

        // Servidor 2: Adiciona cidades e salva em um arquivo JSON
        serverId = "2";
        adicionador = new Arquivo();
        adicionador.adicionarCidade("Salvador", "Rio de Janeiro", 15L, serverId);
        adicionador.adicionarCidade("Sao Paulo", "Salvador", 20L, serverId);
        salvar("cidadesServer2.json");

        // Servidor 3: Adiciona cidades e salva em um arquivo JSON
        serverId = "3";
        adicionador = new Arquivo();
        adicionador.adicionarCidade("Curitiba", "Porto Alegre", 12L, serverId);
        adicionador.adicionarCidade("Manaus", "Belem", 8L, serverId);
        salvar("cidadesServer3.json");

        // Caminho e nome do arquivo JSON para leitura
        String caminhoPasta = "demo/dados";
        String nomeArquivo = "cidadesServer1.json";
        File arquivoJSON = new File(caminhoPasta, nomeArquivo);

        // Chama método para ler o conteúdo do arquivo JSON
        ler_cidades(arquivoJSON);
    }

    // Método para salvar o mapa de trechos em um arquivo JSON
    public static void salvar(String nomedoArquivo) {
        String caminhoPasta = "demo/dados"; // Caminho da pasta de destino
        File arquivoJSON = new File(caminhoPasta, nomedoArquivo);

        // Cria a pasta, se não existir
        File pasta = new File(caminhoPasta);
        if (!pasta.exists()) {
            if (pasta.mkdirs()) {
                System.out.println("Pasta criada com sucesso.");
            } else {
                System.out.println("Falha ao criar a pasta.");
                return;
            }
        }

        // Converte o ConcurrentHashMap em JSONObject para salvar como JSON
        JSONObject jsonObject = new JSONObject(trechos);

        try (FileWriter file = new FileWriter(arquivoJSON)) {
            // Escreve o JSONObject no arquivo
            file.write(jsonObject.toString());
            file.flush();
            System.out.println("HashMap salvo no arquivo JSON com sucesso em: " + arquivoJSON.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para ler um arquivo JSON e carregar seu conteúdo em um mapa
    public static void ler_cidades(File arquivoJSON) throws ParseException, IOException {
        JSONParser parser = new JSONParser();  // Parser para converter JSON em objetos Java
        ConcurrentHashMap<String, Map<String, Map<String, Long>>> vindo_arquivo;

        try (FileReader leitor = new FileReader(arquivoJSON)) {
            // Faz o parsing do conteúdo JSON no arquivo
            JSONObject jsonObject = (JSONObject) parser.parse(leitor);
            vindo_arquivo = new ConcurrentHashMap<>();

            // Popula o mapa vindo_arquivo com os dados do JSON
            vindo_arquivo.putAll(jsonObject);

            // Exibe o conteúdo do mapa carregado
            System.out.println(vindo_arquivo);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
