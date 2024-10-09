package com.example.demo.model;

public class Trecho {
    private String origem;
    private String destino;
    private long passagensDisponiveis;
    private String servidor;

    public Trecho(String origem, String destino, long passagensDisponiveis,String servidor) {
        this.origem = origem;
        this.destino = destino;
        this.passagensDisponiveis = passagensDisponiveis;
        this.servidor = servidor;
    }

    public String getServidor(){
        return servidor;
    }

    public void setServidor(String servidor){
        this.servidor = servidor;
    }


    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public long getPassagensDisponiveis() {
        return passagensDisponiveis;
    }

    public void setPassagensDisponiveis(long passagensDisponiveis) {
        this.passagensDisponiveis = passagensDisponiveis;
    }
}
