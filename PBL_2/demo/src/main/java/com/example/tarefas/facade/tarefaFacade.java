package com.example.tarefas.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.tarefas.dto.tarefaDTO;

@Service
public class tarefaFacade {
    
    private static final HashMap<Integer,tarefaDTO> tarefas = new HashMap<>();


    public tarefaDTO create(tarefaDTO tarefadto){
        int proximo = tarefas.keySet().size() + 1;
        tarefadto.setId(proximo);
        tarefas.put(proximo, tarefadto);
        return tarefadto;
    }


    public tarefaDTO update(tarefaDTO tarefadto){
        tarefas.put(tarefadto.getId(), tarefadto);
        return tarefadto;
    }

    public tarefaDTO getById(Integer id){
        return tarefas.get(id);
    }

    public List<tarefaDTO> getAll(){
        return new ArrayList<>(tarefas.values());
    }

    public String delete(Integer tarefaID){
        tarefas.remove(tarefaID);
        return "DELETADO";
    }



}
