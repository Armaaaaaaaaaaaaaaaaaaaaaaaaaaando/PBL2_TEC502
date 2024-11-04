package com.example.demo.cliente;

import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import com.example.demo.model.Trecho;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Arrays;

public class Cliente {

    private final RestTemplate restTemplate;
    private final String servidorUrl;
    private static final List<String> cidades = Arrays.asList(
        "Brasilia", "Rio de Janeiro", "Sao Paulo", "Salvador", 
        "Curitiba", "Manaus", "Belem", "Porto Alegre"
    );

    public Cliente(String servidorUrl) {
        this.restTemplate = new RestTemplate();
        this.servidorUrl = servidorUrl;
    }

    // Método para listar todas as rotas
    public void todasAsRotas() {
        String url = servidorUrl + "/api/trechos";
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            System.out.println("Resultado da pesquisa: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Erro ao tentar pesquisar: " + e.getMessage());
        }
    }

    // Método para comprar passagem
    public void comprarPassagem(List<Trecho> rotaEscolhida) {
        String url = servidorUrl + "/api/comprar";
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, rotaEscolhida, String.class);
            System.out.println("Resultado da compra: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Erro ao tentar comprar: " + e.getMessage());
        }
    }

    // Método para montar a rota
    public List<List<Trecho>> montarRota(String origem, String destino) {
        String url = servidorUrl + "/api/montarRota?origem=" + origem + "&destino=" + destino;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);

            // Deserializando a resposta para List<List<Trecho>>
            ObjectMapper objectMapper = new ObjectMapper();
            List<List<Trecho>> rotas = objectMapper.readValue(response.getBody(), new TypeReference<List<List<Trecho>>>() {});

            return rotas;
        } catch (Exception e) {
            System.out.println("Erro ao tentar montar a rota: " + e.getMessage());
        }
        return null;
    }

    // Método para realizar múltiplas compras simultâneas
    public void realizarComprasSimultaneas(List<Trecho> rotaEscolhida, int numeroDeCompras) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < numeroDeCompras; i++) {
            int finalI = i;
            executorService.submit(() -> {
                System.out.println("Realizando compra #" + (finalI + 1));
                comprarPassagem(rotaEscolhida);
            });
        }
        executorService.shutdown();
    }

    public static void main(String[] args) {
        // Obtém o URL do servidor a partir da variável de ambiente SERVER_URL, com fallback para um valor padrão
        String servidorUrl = System.getenv("SERVER_URL");
        if (servidorUrl == null || servidorUrl.isEmpty()) {
            servidorUrl = "http://servidor1:8081"; // URL padrão se a variável de ambiente não estiver definida
        }
    
        Cliente cliente = new Cliente(servidorUrl);
        Scanner scanner = new Scanner(System.in);
    
        while (true) {
            try {
                // Limpa o terminal
                limparTerminal();
    
                // Exibir todas as rotas
                cliente.todasAsRotas();
    
                // Mostrar lista de cidades para escolha de origem
                System.out.println("Escolha a cidade de origem:");
                for (int i = 0; i < cidades.size(); i++) {
                    System.out.println(i + ": " + cidades.get(i));
                }
                System.out.print("Digite o número da cidade de origem: ");
                int escolhaOrigem = scanner.nextInt();
                scanner.nextLine(); // Consumir a quebra de linha
                String origem = cidades.get(escolhaOrigem);
    
                // Mostrar lista de cidades para escolha de destino
                System.out.println("Escolha a cidade de destino:");
                for (int i = 0; i < cidades.size(); i++) {
                    System.out.println(i + ": " + cidades.get(i));
                }
                System.out.print("Digite o número da cidade de destino: ");
                int escolhaDestino = scanner.nextInt();
                scanner.nextLine(); // Consumir a quebra de linha
                String destino = cidades.get(escolhaDestino);
    
                // Montar a rota
                List<List<Trecho>> rotasDisponiveis = cliente.montarRota(origem, destino);
    
                // Exibir as rotas disponíveis
                if (rotasDisponiveis != null && !rotasDisponiveis.isEmpty()) {
                    System.out.println("Rotas disponíveis:");
                    for (int i = 0; i < rotasDisponiveis.size(); i++) {
                        System.out.println("Rota " + i + ": " + rotasDisponiveis.get(i));
                    }
    
                    // Solicitar escolha da rota
                    System.out.print("Escolha uma rota (número): ");
                    int escolhaRota = scanner.nextInt();
                    scanner.nextLine();
    
                    // Verificar escolha válida e realizar a compra
                    if (escolhaRota >= 0 && escolhaRota < rotasDisponiveis.size()) {
                        List<Trecho> rotaEscolhida = rotasDisponiveis.get(escolhaRota);
                        cliente.comprarPassagem(rotaEscolhida);
                    } else {
                        System.out.println("Escolha inválida. Tente novamente.");
                    }
                } else {
                    System.out.println("Nenhuma rota disponível. Tente novamente.");
                }
    
                // Perguntar ao usuário se deseja repetir o processo
                System.out.print("Deseja realizar outra operação? (s/n): ");
                String resposta = scanner.nextLine();
                if (!resposta.equalsIgnoreCase("s")) {
                    break;
                }
            } catch (Exception e) {
                System.out.println("Ocorreu um erro: " + e.getMessage() + ". Reiniciando o processo.");
            }
        }
    
        scanner.close();
    }
    
    // Método para simular a limpeza do terminal
    private static void limparTerminal() {
        try {
            // Verifica o sistema operacional e executa o comando adequado
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J"); // Escape ANSI para limpar o terminal no Linux/macOS
                System.out.flush();
            }
        } catch (Exception e) {
            // Caso não seja possível limpar o terminal, imprime linhas vazias
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }
    
}
