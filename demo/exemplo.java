package com.example.demo;



import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.example.demo.AdicionarCidades;

@SpringBootApplication
public class Server1 {

    public static void main(String[] args) {
        new SpringApplicationBuilder(Server1.class)
                .properties("server.port=8081", "server.id=1")
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Bean
    public CommandLineRunner loadData1(ApplicationContext context, Environment env) {
        return args -> {
            AdicionarCidades adicionarCidades = context.getBean(AdicionarCidades.class);
            String nomeArquivo = "cidadesServer1.json";
            File arquivoJSON = new File("dados", nomeArquivo);
            System.out.printf("Servidor 1 ativado, carregando: %s%n", nomeArquivo);
            ConcurrentHashMap<String, Map<String, Map<String, Long>>> cidades = ler_cidades(arquivoJSON);

            if (cidades != null && !cidades.isEmpty()) {
                adicionar_cidades_no_servidor(cidades, adicionarCidades);
            } else {
                System.err.printf("Nenhuma cidade encontrada no arquivo %s ou erro ao carregar o arquivo.%n", nomeArquivo);
            }
        };
    }

    public ConcurrentHashMap<String, Map<String, Map<String, Long>>> ler_cidades(File arquivoJSON) {
        JSONParser parser = new JSONParser();
        ConcurrentHashMap<String, Map<String, Map<String, Long>>> trechos_do_arquivo = new ConcurrentHashMap<>();

        try (FileReader leitor = new FileReader(arquivoJSON)) {
            JSONObject jsonObject = (JSONObject) parser.parse(leitor);
            trechos_do_arquivo.putAll(jsonObject); // Popula o mapa
        } catch (IOException | ParseException e) {
            System.err.printf("Erro ao ler o arquivo %s: %s%n", arquivoJSON.getName(), e.getMessage());
        }
        return trechos_do_arquivo;
    }

    public void adicionar_cidades_no_servidor(ConcurrentHashMap<String, Map<String, Map<String, Long>>> cidades, AdicionarCidades adicionarCidades) {
        if (cidades != null) {
            for (Map.Entry<String, Map<String, Map<String, Long>>> origem : cidades.entrySet()) {
                String cidade_origem = origem.getKey();
                for (Map.Entry<String, Map<String, Long>> destino : origem.getValue().entrySet()) {
                    String cidade_destino = destino.getKey();
                    for (Map.Entry<String, Long> id : destino.getValue().entrySet()) {
                        String serverID = id.getKey();
                        Long Qnt_passagens = id.getValue();
                        adicionarCidades.adicionarCidade(cidade_origem, cidade_destino, Qnt_passagens, serverID);
                    }
                }
            }
            System.out.println("Adicionado com sucesso ao servidor 1!!!!");
        }
    }
}
