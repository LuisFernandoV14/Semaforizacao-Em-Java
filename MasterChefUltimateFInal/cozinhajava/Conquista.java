public class Conquista {
    private final String nome;
    private final String descricao;
    private final String icone;
    private final int pontosNecessarios;
    public boolean desbloqueada;
    
    public Conquista(String nome, String descricao, String icone, int pontosNecessarios) {
        this.nome = nome;
        this.descricao = descricao;
        this.icone = icone;
        this.pontosNecessarios = pontosNecessarios;
        this.desbloqueada = false;
    }
    
    public boolean verificar(int pontuacaoAtual) {
        if (!desbloqueada && pontuacaoAtual >= pontosNecessarios) {
            desbloqueada = true;
            return true;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return String.format("%s %s - %s", icone, nome, descricao);
    }
} 