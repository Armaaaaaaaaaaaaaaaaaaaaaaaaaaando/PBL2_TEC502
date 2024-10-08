package com.example.demo;

import com.example.demo.model.Trecho;
import com.example.demo.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdicionarCidades {

    @Autowired
    private CompraService compraService;

    public void adicionarCidade(String origem, String destino, Long passagensDisponiveis) {
        Trecho trecho = new Trecho(origem, destino, passagensDisponiveis);
        String chave = origem + "-" + destino;
        compraService.getAllTrechos().put(chave, trecho);
    }
}
