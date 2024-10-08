package com.example.demo.controller;

import com.example.demo.model.Trecho;
import com.example.demo.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api")
public class ServidorController {

    @Autowired
    private CompraService compraService;

    @GetMapping("/trechos")
    public ConcurrentHashMap<String, Trecho> listarTrechos() {
        return compraService.getAllTrechos();
    }
    

    @PostMapping("/comprar")
    public String comprarPassagem(@RequestParam String origem, @RequestParam String destino) {
        if (compraService.comprarPassagem(origem, destino)) {
            return "Passagem comprada com sucesso!";
        } else {
            return "Falha na compra: passagens esgotadas ou trecho inexistente.";
        }
    }

    // Endpoint para receber a atualização do trecho
    @PutMapping("/atualizarTrecho")
    public String atualizarTrecho(@RequestBody Map<String, Object> trechoData) {
        String origem = (String) trechoData.get("origem");
        String destino = (String) trechoData.get("destino");
        int passagensDisponiveis = (Integer) trechoData.get("passagensDisponiveis");

        Trecho trecho = compraService.getTrecho(origem, destino);
        
        if (trecho != null) {
            // Atualiza o número de passagens disponíveis localmente
            trecho.setPassagensDisponiveis(passagensDisponiveis);
            return "Trecho atualizado localmente com sucesso!";
        } else {
            return "Falha ao atualizar trecho: trecho inexistente.";
        }
    }
}
