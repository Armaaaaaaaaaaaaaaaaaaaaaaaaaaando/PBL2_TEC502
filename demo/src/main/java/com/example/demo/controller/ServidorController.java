package com.example.demo.controller;

import com.example.demo.model.Trecho;
import com.example.demo.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.ApplicationContext;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@RestController
@RequestMapping("/api")
public class ServidorController {
    private final ApplicationContext context;

    @Autowired
    public ServidorController(ApplicationContext context) {
        this.context = context;
    }

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
public String comprarPassagem(@RequestBody List<Trecho> rotaEscolhida) {
    if (rotaEscolhida == null || rotaEscolhida.isEmpty()) {
        return "Falha: A rota escolhida não pode ser vazia.";
    }

    return compraService.comprar(rotaEscolhida);
}


    @GetMapping("/liberarPermissao")
    public void liberarPermissao(@RequestParam int servidorId) {
        compraService.liberarToken(); // Libera o token para o próximo servidor
    }

    @GetMapping("/heartbeat")
    public ResponseEntity<String> heartbeat() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/receberToken")
    public void receberToken() {
        compraService.receberToken(); // Recebe o token
    }

    @PostMapping("/comprarTrecho")
    public String comprarTrecho(@RequestParam String origem, @RequestParam String destino) {
        return compraService.comprarTrechoIndividual(origem, destino);
    }

    @PostMapping("/atualizarTrecho")
    public ResponseEntity<String> atualizarTrecho(@RequestBody Trecho trecho) {
        compraService.getAll().put(trecho.getOrigem() + "-" + trecho.getDestino(), trecho);
        return ResponseEntity.ok("Trecho atualizado com sucesso.");
    }
    @GetMapping("/shutdown")
    public String shutdownServer() {
        System.out.println("Servidor desligado via endpoint /shutdown");
        SpringApplication.exit(context, () -> 0);
        return "Servidor desligando...";
    }

    @PostMapping("/montarRota")
    public ResponseEntity<List<List<Trecho>>> montarRota(@RequestParam String origem, @RequestParam String destino) {
        List<List<Trecho>> rotas = compraService.montarRota(origem, destino);
        return ResponseEntity.ok(rotas);
    }
}
