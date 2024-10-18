package com.example.demo.controller;

import com.example.demo.model.Trecho;
import com.example.demo.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/trecho")
    public ConcurrentHashMap<String, Trecho> geta() {
        return compraService.getAll();
    }
    
    @GetMapping("/ready")
    public String ready() {
        return "OK";
    }

    @PostMapping("/comprar")
    public String comprarPassagem(@RequestParam String origem, @RequestParam String destino) {
        return compraService.comprar(origem, destino);
    }

    @GetMapping("/liberarPermissao")
    public void liberarPermissao(@RequestParam int servidorId) {
        compraService.liberarToken(); // Libera o token para o próximo servidor
    }
    
    @GetMapping("/receberToken")
    public void receberToken() {
        compraService.receberToken(); // Recebe o token
    }

    @PostMapping("/atualizarTrecho")
    public ResponseEntity<String> atualizarTrecho(@RequestBody Trecho trecho) {
        compraService.getAll().put(trecho.getOrigem() + "-" + trecho.getDestino(), trecho);
        return ResponseEntity.ok("Trecho atualizado com sucesso.");
    }
}
