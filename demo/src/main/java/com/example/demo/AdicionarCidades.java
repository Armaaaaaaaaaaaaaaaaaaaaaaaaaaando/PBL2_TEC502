package com.example.demo;

import com.example.demo.model.Trecho;
import com.example.demo.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Indica que esta classe é um componente gerenciado pelo Spring, podendo ser injetado em outros lugares
@Component
public class AdicionarCidades {

    // Injeção de dependência do serviço de compra, que contém a lógica para manipular os trechos
    @Autowired
    private CompraService compraService;

    // Método para adicionar uma nova cidade (trecho) ao sistema, com origem, destino, passagens disponíveis e servidor
    public void adicionarCidade(String origem, String destino, Long passagensDisponiveis, String servidor) {
        // Cria um novo objeto Trecho com os parâmetros fornecidos
        Trecho trecho = new Trecho(origem, destino, passagensDisponiveis, servidor);

        // Gera uma chave única para o trecho com base na combinação origem-destino
        String chave = origem + "-" + destino;

        // Adiciona o trecho ao mapa de trechos no serviço de compra
        compraService.getAll().put(chave, trecho);
    }
}
