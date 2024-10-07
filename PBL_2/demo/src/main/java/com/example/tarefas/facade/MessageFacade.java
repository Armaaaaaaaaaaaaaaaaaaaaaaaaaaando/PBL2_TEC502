package com.example.tarefas.facade;

import com.example.tarefas.dto.MessageDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class MessageFacade {
    private static final HashMap <Integer,MessageDTO> mensagens = new HashMap<>();


    public MessageDTO criar(MessageDTO mensagemdto){
        int proximo = mensagens.keySet().size() + 1;
        mensagemdto.setSenderId(proximo);
        mensagens.put(proximo, mensagemdto);

        return mensagemdto; 
    }


    public MessageDTO atualizar(MessageDTO mensagemdto){
        mensagens.put(mensagemdto.getSenderId(), mensagemdto);
        return mensagemdto;
    }

    public List<MessageDTO> getAll(){
        return new ArrayList<>(mensagens.values());
    }

    public String deletar(Integer idMensagem){
        mensagens.remove(idMensagem);
        return "DELETADO!";
    }
}
