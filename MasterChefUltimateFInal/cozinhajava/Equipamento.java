public class Equipamento {
    private final String nome;
    private final String icone;
    private int durabilidade;
    private boolean ligado;
    private double eficiencia;
    private double temperatura;
    private boolean precisaManutencao;
    
    public Equipamento(String nome, String icone) {
        this.nome = nome;
        this.icone = icone;
        this.durabilidade = 100;
        this.ligado = false;
        this.eficiencia = 1.0;
        this.temperatura = 25.0;
        this.precisaManutencao = false;
    }
    
    public void ligar() {
        if (!precisaManutencao) {
            ligado = true;
            temperatura += 10;
            durabilidade -= 1;
            if (durabilidade < 30) {
                eficiencia = 0.5;
            }
            if (durabilidade < 10) {
                precisaManutencao = true;
                ligado = false;
            }
        }
    }
    
    public void desligar() {
        ligado = false;
        if (temperatura > 25) {
            temperatura -= 5;
        }
    }
    
    public void fazerManutencao() {
        durabilidade = 100;
        eficiencia = 1.0;
        precisaManutencao = false;
        temperatura = 25.0;
    }
    
    public String getNome() {
        return nome;
    }
    
    public String getIcone() {
        return icone;
    }
    
    public int getDurabilidade() {
        return durabilidade;
    }
    
    public boolean isLigado() {
        return ligado;
    }
    
    public double getEficiencia() {
        return eficiencia;
    }
    
    public double getTemperatura() {
        return temperatura;
    }
    
    public boolean isPrecisaManutencao() {
        return precisaManutencao;
    }
    
    public String getEstadoDescricao() {
        if (precisaManutencao) return "Precisa Manutenção 🔧";
        if (durabilidade < 30) return "Desgastado ⚠️";
        if (durabilidade < 70) return "Regular 😐";
        return "Bom Estado 👍";
    }
    
    @Override
    public String toString() {
        return String.format("%s %s (%d%% - %s)", 
            icone, nome, durabilidade, getEstadoDescricao());
    }
} 