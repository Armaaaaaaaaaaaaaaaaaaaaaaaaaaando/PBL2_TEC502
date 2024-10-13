

public class MensagemLamport {
    private int idServidor;      
    private int relogioLogico;   
    private String infoCompra;   // Informações da compra (por exemplo, passagem)
    private String status;       // Status da requisição ("OK" ou "Não OK")
    
    // Construtor
    public MensagemLamport(int idServidor, int relogioLogico, String infoCompra) {
        this.idServidor = idServidor;
        this.relogioLogico = relogioLogico;
        this.infoCompra = infoCompra;
        this.status = "Pendente";  // Status inicial
    }
    
    // Getters e Setters
    public int getIdServidor() {
        return idServidor;
    }

    public void setIdServidor(int idServidor) {
        this.idServidor = idServidor;
    }

    public int getRelogioLogico() {
        return relogioLogico;
    }

    public void setRelogioLogico(int relogioLogico) {
        this.relogioLogico = relogioLogico;
    }

    public String getInfoCompra() {
        return infoCompra;
    }

    public void setInfoCompra(String infoCompra) {
        this.infoCompra = infoCompra;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MensagemLamport{" +
                "idServidor=" + idServidor +
                ", relogioLogico=" + relogioLogico +
                ", infoCompra='" + infoCompra + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
