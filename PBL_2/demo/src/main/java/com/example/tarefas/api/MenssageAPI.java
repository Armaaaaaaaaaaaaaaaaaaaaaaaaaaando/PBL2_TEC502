package com.example.tarefas.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.example.tarefas.dto.MessageDTO;
import com.example.tarefas.facade.MessageFacade;
import java.util.List;
import org.springframework.http.MediaType;

@Controller
@RequestMapping(value = "/api/mensagens", produces = MediaType.APPLICATION_JSON_VALUE)
public class MenssageAPI {

    @Autowired
    private MessageFacade messageFacade; // Corrigido o nome da variável

    @PostMapping
    @ResponseBody
    public MessageDTO criar(@RequestBody MessageDTO mensagemdto) {
        return messageFacade.criar(mensagemdto);
    }

    @PutMapping("/{mensagemID}")
    @ResponseBody
    public MessageDTO update(@PathVariable("mensagemID") Integer mensagemID, @RequestBody MessageDTO mensagemdto) {
        mensagemdto.setSenderId(mensagemID); // Corrigido o uso do ID no setter
        return messageFacade.atualizar(mensagemdto);
    }

    @GetMapping
    @ResponseBody
    public List<MessageDTO> getAll() {
        return messageFacade.getAll();
    }

    @DeleteMapping("/{mensagemID}") // Corrigido o nome da variável no @PathVariable
    @ResponseBody
    public String delete(@PathVariable("mensagemID") Integer mensagemID) { // Corrigido o nome da variável
        return messageFacade.deletar(mensagemID);
    }
}
