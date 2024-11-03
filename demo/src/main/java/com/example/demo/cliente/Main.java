package com.example.demo.cliente;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        String servidorUrl = "http://localhost:8081"; // URL do servidor
        String origem = "Sao Paulo";
        String destino = "Brasilia"; 

        int numeroDeClientes = 5; // Número de clientes q vai rodar
        
        ExecutorService executorService = Executors.newFixedThreadPool(numeroDeClientes);

        for (int i = 0; i < numeroDeClientes; i++) {
            ClienteRunnable clienteRunnable = new ClienteRunnable(servidorUrl,origem,destino);
            executorService.submit(clienteRunnable); // Envia a tarefa para execução
        }

        executorService.shutdown(); // Finaliza o executor
    }
}
