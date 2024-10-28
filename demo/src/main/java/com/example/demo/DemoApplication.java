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
    private static ConcurrentHashMap<String, Map<String, Map<String, Long>>>  trechos_do_arquivo; 

    public static void main(String[] args) {
        startServer("1", 8081);
        startServer("3", 8083);
        startServer("2", 8082);

    }

    private static void startServer(String serverId, int port) {
        new SpringApplicationBuilder(DemoApplication.class)
                .properties("server.port=" + port, "server.id=" + serverId)
                .web(WebApplicationType.SERVLET)
                .run();
    }

    @Bean
    public CommandLineRunner loadData(ApplicationContext context, Environment env) {
        return args -> {
            // Obtém o ID do servidor a partir do ambiente (env)
            String serverId = env.getProperty("server.id");
            AdicionarCidades adicionarCidades = context.getBean(AdicionarCidades.class);
            

            ConcurrentHashMap<String, Map<String, Map<String, Long>>>  cidades_servidor_1 = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, Map<String, Map<String, Long>>>  cidades_servidor_2 = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, Map<String, Map<String, Long>>>  cidades_servidor_3 = new ConcurrentHashMap<>();


            // Caminho e nome do arquivo JSON
            String caminhoPasta = "dados";

            if(serverId.equals("1")){
                String nomeArquivo = "cidadesServer1.json";
                File arquivoJSON = new File(caminhoPasta, nomeArquivo);
                cidades_servidor_1 = ler_cidades(arquivoJSON);

                //adicionando cidades do primeiro servidor
                adicionar_cidades_no_servidor(cidades_servidor_1,adicionarCidades);
            }
            else if(serverId.equals("2")){
                String nomeArquivo = "cidadesServer2.json";
                File arquivoJSON = new File(caminhoPasta, nomeArquivo);
                cidades_servidor_2 = ler_cidades(arquivoJSON);
                //adicionando cidades no segundo servidor
                adicionar_cidades_no_servidor(cidades_servidor_2,adicionarCidades);
            }

            else if(serverId.equals("3")){
                String nomeArquivo = "cidadesServer3.json";
                File arquivoJSON = new File(caminhoPasta, nomeArquivo);
                cidades_servidor_3 = ler_cidades(arquivoJSON);
                //adicionando cidades no terceiro servidor
                adicionar_cidades_no_servidor(cidades_servidor_3,adicionarCidades);
            }
            
        };
    }


    public static  ConcurrentHashMap<String, Map<String, Map<String, Long>>>  ler_cidades(File arquivoJSON) throws ParseException, IOException {
        JSONParser parser = new JSONParser();  // Sem argumentos no construtor
        
         
        try (FileReader leitor = new FileReader(arquivoJSON)) {
            JSONObject jsonObject = (JSONObject) parser.parse(leitor);
            trechos_do_arquivo = new ConcurrentHashMap<>();

            
            trechos_do_arquivo.putAll(jsonObject);

            
            System.out.println(trechos_do_arquivo);

        }
        catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        return trechos_do_arquivo;
    }


    public void adicionar_cidades_no_servidor(ConcurrentHashMap<String, Map<String, Map<String, Long>>> cidades,AdicionarCidades adicionarCidades){
        if(cidades!= null){
            for(Map.Entry<String, Map<String, Map<String, Long>>> origem:  trechos_do_arquivo.entrySet()){
                String cidade_origem = origem.getKey();
                //tentar ver size para corrijir as cidades vazias
                Map<String, Map<String, Long>> segundoMapa = origem.getValue();
                for(Map.Entry<String, Map<String, Long>> destino : segundoMapa.entrySet()){
                    String cidade_destino = destino.getKey();
                    Map<String, Long> terceiroMapa = destino.getValue();

                    for (Map.Entry<String, Long> id : terceiroMapa.entrySet()){
                        
                        String serverID = id.getKey();
                        Long Qnt_passagens = id.getValue();
                        
                        adicionarCidades.adicionarCidade(cidade_origem, cidade_destino, Qnt_passagens, serverID);
                        
                    }
                    
                }
                    

            }
            System.out.println("adicionado com sucesso ao servidor!!!!");
        }
    }


}
