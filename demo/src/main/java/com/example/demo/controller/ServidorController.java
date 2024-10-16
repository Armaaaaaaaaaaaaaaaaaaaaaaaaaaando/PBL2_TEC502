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

    @GetMapping("/solicitarPermissao")
    public void solicitarPermissao(@RequestParam int servidorId, @RequestParam int clock) {
        compraService.receberPermissao(servidorId, clock);
    }

    @GetMapping("/liberarPermissao")
    public void liberarPermissao(@RequestParam int servidorId) {
    }
    @PostMapping("/ack")
public ResponseEntity<String> receberAck(@RequestBody Map<String, Integer> ackData) {
    Integer servidorId = ackData.get("servidorId");
    Integer clock = ackData.get("clock");

    System.out.println("ACK recebido de servidorId: " + servidorId + ", clock: " + clock);

    return ResponseEntity.ok("ACK recebido");
}

}
