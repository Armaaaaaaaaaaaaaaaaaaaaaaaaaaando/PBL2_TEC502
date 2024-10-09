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
            return compraService.comprar(origem,destino);

           // return "Falha na compra: passagens esgotadas ou trecho inexistente.";
        }
    

}
