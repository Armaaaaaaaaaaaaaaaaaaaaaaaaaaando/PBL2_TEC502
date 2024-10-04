package com.example.tarefas.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType; // Importação correta
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.tarefas.dto.tarefaDTO;
import com.example.tarefas.facade.tarefaFacade;

@Controller
@RequestMapping(value = "/tarefas", produces = MediaType.APPLICATION_JSON_VALUE)
public class tarefaAPI {
    @Autowired
    private tarefaFacade farefasfacade;

    @PostMapping
    @ResponseBody
    public tarefaDTO create(@RequestBody tarefaDTO tarefadto) {
        return farefasfacade.create(tarefadto);
    }

    @PutMapping("/{tarefaID}") // Corrigido o mapeamento do caminho
    @ResponseBody
    public tarefaDTO update(@PathVariable("tarefaID") Integer tarefaID, @RequestBody tarefaDTO tarefadto) {
        tarefadto.setId(tarefaID); 
        return farefasfacade.update(tarefadto);
    }

    @GetMapping
    @ResponseBody
    public List<tarefaDTO> getAll() {
        return farefasfacade.getAll();
    }

    @DeleteMapping("/{tarefaID}")
    @ResponseBody
    public String delete(@PathVariable("tarefaID") Integer tarefaID) {
        return farefasfacade.delete(tarefaID);
    }
}
