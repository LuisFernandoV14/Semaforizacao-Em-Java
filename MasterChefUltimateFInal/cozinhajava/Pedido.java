import java.util.*;

public class Pedido {
    public enum StatusPedido {
        PENDENTE, EM_PREPARO, CONCLUIDO
    }
    private final String cliente;
    private final Receita receita;
    private final long tempoInicio;
    private final int tempoMaximo;
    private boolean concluido;
    private double avaliacao;
    private String comentario;
    private final int gorjeta;
    private final boolean clienteVip;
    private StatusPedido status;
    private Chef chefDesignado;
    
    public Pedido(String cliente, Receita receita, int tempoMaximo, boolean vip) {
        this.cliente = cliente;
        this.receita = receita;
        this.tempoInicio = System.currentTimeMillis();
        this.tempoMaximo = tempoMaximo;
        this.concluido = false;
        this.avaliacao = 0;
        this.comentario = "";
        this.gorjeta = new Random().nextInt(20) + 10; // 10-30% de gorjeta
        this.clienteVip = vip;
        setStatus(StatusPedido.PENDENTE);
        this.chefDesignado = null;
        System.out.println(String.format("[Pedido %s] Criado. Status: %s", cliente, this.status));
    }
    
    public void concluir(double qualidadeReceita) {
        System.out.println(String.format("[Pedido %s] Iniciando concluir(). Status ANTES: %s", cliente, this.status));
        concluido = true;
        long tempoGasto = (System.currentTimeMillis() - tempoInicio) / 1000;
        
        // Calcula avaliação base na qualidade
        avaliacao = qualidadeReceita / 20; // 0-5 estrelas
        
        // Ajusta baseado no tempo
        if (tempoGasto > tempoMaximo) {
            avaliacao -= (tempoGasto - tempoMaximo) / 60.0; // Perde 1 estrela por minuto de atraso
        } else {
            avaliacao += 0.5; // Bônus por entrega no prazo
        }
        
        // Limita entre 0-5 estrelas
        avaliacao = Math.max(0, Math.min(5, avaliacao));
        
        // Define comentário baseado na avaliação
        if (avaliacao >= 4.5) {
            comentario = "Excelente! Superou minhas expectativas! ⭐⭐⭐⭐⭐";
        } else if (avaliacao >= 4.0) {
            comentario = "Muito bom! Voltarei mais vezes! ⭐⭐⭐⭐";
        } else if (avaliacao >= 3.0) {
            comentario = "Bom, mas pode melhorar. ⭐⭐⭐";
        } else if (avaliacao >= 2.0) {
            comentario = "Regular, esperava mais... ⭐⭐";
        } else {
            comentario = "Decepcionante. Não recomendo. ⭐";
        }
        
        // Cliente VIP é mais exigente
        if (clienteVip && avaliacao < 4.0) {
            comentario += " (Cliente VIP insatisfeito!)";
        }
        
        setStatus(StatusPedido.CONCLUIDO);
        System.out.println(String.format("[Pedido %s] concluir() finalizado. Status DEPOIS: %s, Avaliação: %.1f", cliente, this.status, avaliacao));
    }
    
    public String getCliente() {
        return cliente;
    }
    
    public Receita getReceita() {
        return receita;
    }
    
    public long getTempoRestante() {
        if (concluido) return 0;
        long tempoGasto = (System.currentTimeMillis() - tempoInicio) / 1000;
        return Math.max(0, tempoMaximo - tempoGasto);
    }
    
    public boolean isConcluido() {
        return concluido;
    }
    
    public double getAvaliacao() {
        return avaliacao;
    }
    
    public String getComentario() {
        return comentario;
    }
    
    public int getGorjeta() {
        return gorjeta;
    }
    
    public boolean isClienteVip() {
        return clienteVip;
    }
    
    public int calcularPontuacaoTotal() {
        if (!concluido) return 0;
        
        int pontos = receita.getPontos();
        
        // Bônus por avaliação
        pontos += (int)(avaliacao * 20);
        
        // Bônus por gorjeta
        pontos += gorjeta * 2;
        
        // Bônus por cliente VIP
        if (clienteVip) {
            pontos *= 1.5;
        }
        
        return pontos;
    }
    
    public void designarChef(Chef chef) {
        this.chefDesignado = chef;
    }
    
    public StatusPedido getStatus() { return status; }
    public Chef getChefDesignado() { return chefDesignado; }
    
    public void setStatus(StatusPedido novoStatus) {
        StatusPedido statusAntigo = this.status;
        this.status = novoStatus;
        System.out.println(String.format("[Pedido %s] Status alterado de %s para %s", cliente, statusAntigo, this.status));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(clienteVip ? "👑 " : "").append(cliente);
        sb.append(" - ").append(receita.getNome());
        
        if (!concluido) {
            sb.append(String.format("\nTempo restante: %d segundos", getTempoRestante()));
        } else {
            sb.append(String.format("\nAvaliação: %.1f ⭐", avaliacao));
            sb.append("\nComentário: ").append(comentario);
            sb.append(String.format("\nGorjeta: %d%%", gorjeta));
            sb.append(String.format("\nPontos: %d", calcularPontuacaoTotal()));
        }
        
        return sb.toString();
    }
} 