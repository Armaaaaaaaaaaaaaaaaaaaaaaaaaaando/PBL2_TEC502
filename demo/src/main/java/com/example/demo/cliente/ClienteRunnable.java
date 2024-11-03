package com.example.demo.cliente;

import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Trecho;

import java.util.List;
import java.util.Scanner;

public class ClienteRunnable implements Runnable {
    private final String servidorUrl;
    private final String origem;
    private final String destino;
    private List<List<Trecho>> rotasDisponiveis; 



    public ClienteRunnable(String servidorUrl,String origem,String destino) {
        this.servidorUrl = servidorUrl;
        this.destino = destino;
        this.origem = origem;
    }

    @Override
    public void run() {
        Cliente cliente = new Cliente(servidorUrl);
        
        
        // Exibir todas as rotas
        cliente.todasAsRotas();
        rotasDisponiveis = cliente.montarRota(origem, destino);
        cliente.comprarPassagem(rotasDisponiveis.get(0));

        cliente.todasAsRotas();


        
    }
}
