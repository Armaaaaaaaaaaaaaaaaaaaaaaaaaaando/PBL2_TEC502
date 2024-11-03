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

    // Contexto da aplicação, necessário para manipular o ciclo de vida da aplicação (por exemplo, para desligar o servidor).
    private final ApplicationContext context;

    // Construtor da classe, onde o ApplicationContext é injetado.
    @Autowired
    public ServidorController(ApplicationContext context) {
        this.context = context;
    }

    // Injeção de dependência para a classe de serviço que contém as regras de compra.
    @Autowired
    private CompraService compraService;

    // Endpoint para listar todos os trechos disponíveis.
    @GetMapping("/trechos")
    public ConcurrentHashMap<String, Trecho> listarTrechos() {
        return compraService.getAllTrechos();
    }

    // Endpoint para obter todos os trechos (talvez usado para um propósito específico no seu projeto).
    @GetMapping("/trecho")
    public ConcurrentHashMap<String, Trecho> geta() {
        return compraService.getAll();
    }

    // Endpoint para verificar o status de prontidão do servidor.
    @GetMapping("/ready")
    public String ready() {
        return "OK";
    }

    // Endpoint para comprar uma passagem, passando uma lista de trechos.
    @PostMapping("/comprar")
    public String comprarPassagem(@RequestBody List<Trecho> rotaEscolhida) {
        if (rotaEscolhida == null || rotaEscolhida.isEmpty()) {
            return "Falha: A rota escolhida não pode ser vazia.";
        }

        return compraService.comprar(rotaEscolhida);
    }

    // Endpoint para liberar a permissão de compra para o próximo servidor.
    @GetMapping("/liberarPermissao")
    public void liberarPermissao(@RequestParam int servidorId) {
        compraService.liberarToken(); // Libera o token para o próximo servidor.
    }

    // Endpoint para verificar se o servidor está ativo através de um heartbeat.
    @GetMapping("/heartbeat")
    public ResponseEntity<String> heartbeat() {
        return ResponseEntity.ok("OK");
    }

    // Endpoint para receber o token de permissão de compra.
    @GetMapping("/receberToken")
    public void receberToken() {
        compraService.receberToken(); // Recebe o token.
    }

    // Endpoint para comprar um trecho individual com origem e destino especificados.
    @PostMapping("/comprarTrecho")
    public String comprarTrecho(@RequestParam String origem, @RequestParam String destino) {
        return compraService.comprarTrechoIndividual(origem, destino);
    }

    // Endpoint para atualizar as informações de um trecho específico.
    @PostMapping("/atualizarTrecho")
    public ResponseEntity<String> atualizarTrecho(@RequestBody Trecho trecho) {
        compraService.getAll().put(trecho.getOrigem() + "-" + trecho.getDestino(), trecho);
        return ResponseEntity.ok("Trecho atualizado com sucesso.");
    }

    // Endpoint para desligar o servidor.
    @GetMapping("/shutdown")
    public String shutdownServer() {
        System.out.println("Servidor desligado via endpoint /shutdown");
        SpringApplication.exit(context, () -> 0); // Encerra a aplicação.
        return "Servidor desligando...";
    }

    // Endpoint para montar uma rota com base em uma origem e destino, retornando possíveis rotas.
    @PostMapping("/montarRota")
    public ResponseEntity<List<List<Trecho>>> montarRota(@RequestParam String origem, @RequestParam String destino) {
        List<List<Trecho>> rotas = compraService.montarRota(origem, destino);
        return ResponseEntity.ok(rotas);
    }
}
