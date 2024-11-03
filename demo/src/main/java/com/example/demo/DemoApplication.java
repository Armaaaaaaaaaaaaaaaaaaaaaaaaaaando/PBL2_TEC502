package com.example.demo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@SpringBootApplication
public class DemoApplication {
    // Mapa estático para armazenar os trechos lidos dos arquivos JSON
    private static ConcurrentHashMap<String, Map<String, Map<String, Long>>> trechos_do_arquivo;

    public static void main(String[] args) {
        // Obtém o ID do servidor e a porta das variáveis de ambiente
        String serverId = System.getenv("SERVER_ID");
        String port = System.getenv("SERVER_PORT");

        // Verifica se ambas as variáveis de ambiente estão definidas antes de iniciar o servidor
        if (serverId != null && port != null) {
            startServer(serverId, Integer.parseInt(port));
        } else {
            System.err.println("As variáveis de ambiente SERVER_ID e SERVER_PORT devem ser definidas.");
            System.exit(1);
        }
    }

    // Método que inicia o servidor Spring com as propriedades de porta e ID do servidor
    private static void startServer(String serverId, int port) {
        new SpringApplicationBuilder(DemoApplication.class)
                .properties("server.port=" + port, "server.id=" + serverId)
                .web(WebApplicationType.SERVLET)
                .run();
    }

    @Bean
    public CommandLineRunner loadData(ApplicationContext context, Environment env) {
        return args -> {
            // Obtém o ID do servidor do ambiente
            String serverId = env.getProperty("server.id");

            // Instância do componente AdicionarCidades para adicionar cidades ao sistema
            AdicionarCidades adicionarCidades = context.getBean(AdicionarCidades.class);

            // Define o caminho e o nome do arquivo JSON com base no ID do servidor
            String caminhoPasta = "dados";
            String nomeArquivo = String.format("cidadesServer%s.json", serverId);
            File arquivoJSON = new File(caminhoPasta, nomeArquivo);

            System.out.printf("Servidor %s ativado, carregando: %s%n", serverId, nomeArquivo);

            // Carrega as cidades do arquivo JSON
            ConcurrentHashMap<String, Map<String, Map<String, Long>>> cidades = ler_cidades(arquivoJSON);

            // Adiciona as cidades ao servidor se o arquivo JSON contiver dados
            if (!cidades.isEmpty()) {
                adicionar_cidades_no_servidor(cidades, adicionarCidades);
            } else {
                System.err.printf("Nenhuma cidade encontrada no arquivo %s%n", nomeArquivo);
            }
        };
    }

    // Método para ler os dados das cidades a partir de um arquivo JSON
    public static ConcurrentHashMap<String, Map<String, Map<String, Long>>> ler_cidades(File arquivoJSON) throws ParseException, IOException {
        JSONParser parser = new JSONParser();  // Parser para leitura do JSON

        try (FileReader leitor = new FileReader(arquivoJSON)) {
            // Lê o JSON e converte para um ConcurrentHashMap
            JSONObject jsonObject = (JSONObject) parser.parse(leitor);
            trechos_do_arquivo = new ConcurrentHashMap<>();

            trechos_do_arquivo.putAll(jsonObject);

            System.out.println(trechos_do_arquivo);

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return trechos_do_arquivo;
    }

    // Método para adicionar cidades ao servidor usando o mapa lido do JSON
    public void adicionar_cidades_no_servidor(ConcurrentHashMap<String, Map<String, Map<String, Long>>> cidades, AdicionarCidades adicionarCidades) {
        if (cidades != null) {
            for (Map.Entry<String, Map<String, Map<String, Long>>> origem : trechos_do_arquivo.entrySet()) {
                String cidade_origem = origem.getKey();
                Map<String, Map<String, Long>> segundoMapa = origem.getValue();

                // Itera sobre os destinos a partir da cidade de origem
                for (Map.Entry<String, Map<String, Long>> destino : segundoMapa.entrySet()) {
                    String cidade_destino = destino.getKey();
                    Map<String, Long> terceiroMapa = destino.getValue();

                    // Itera sobre o ID do servidor e o número de passagens disponíveis
                    for (Map.Entry<String, Long> id : terceiroMapa.entrySet()) {
                        String serverID = id.getKey();
                        Long Qnt_passagens = id.getValue();

                        // Adiciona a cidade no componente adicionarCidades
                        adicionarCidades.adicionarCidade(cidade_origem, cidade_destino, Qnt_passagens, serverID);
                    }
                }
            }
            System.out.println("adicionado com sucesso ao servidor!!!!");
        }
    }
}
