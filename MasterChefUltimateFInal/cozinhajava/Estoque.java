import java.util.*;

public class Estoque {
    private final Map<Ingrediente, Integer> geladeira;
    private final Map<Ingrediente, Integer> estoqueSeco;
    private final List<Equipamento> equipamentos;
    private double temperatura;
    private boolean energiaLigada;
    private Timer timerDeterioracao;
    
    public Estoque() {
        this.geladeira = new EnumMap<>(Ingrediente.class);
        this.estoqueSeco = new EnumMap<>(Ingrediente.class);
        this.equipamentos = new ArrayList<>();
        this.temperatura = 5.0;
        this.energiaLigada = true;
        
        // Inicializa equipamentos básicos
        equipamentos.add(new Equipamento("Fogão", "🔥"));
        equipamentos.add(new Equipamento("Forno", "🎛️"));
        equipamentos.add(new Equipamento("Geladeira", "❄️"));
        equipamentos.add(new Equipamento("Microondas", "📟"));
        
        // Inicia timer de deterioração
        iniciarTimerDeterioracao();
    }
    
    private void iniciarTimerDeterioracao() {
        timerDeterioracao = new Timer();
        timerDeterioracao.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!energiaLigada) {
                    temperatura += 0.5;
                    if (temperatura > 15) {
                        deteriorarTudo();
                    }
                }
                deteriorarGeladeira();
                deteriorarEstoque();
            }
        }, 10000, 10000); // A cada 10 segundos
    }
    
    public void adicionarIngrediente(Ingrediente ingrediente, int quantidade, boolean refrigerar) {
        Map<Ingrediente, Integer> destino = refrigerar ? geladeira : estoqueSeco;
        destino.put(ingrediente, destino.getOrDefault(ingrediente, 0) + quantidade);
        ingrediente.setRefrigerado(refrigerar);
    }
    
    public boolean retirarIngrediente(Ingrediente ingrediente, int quantidade) {
        Map<Ingrediente, Integer> origem = ingrediente.isRefrigerado() ? geladeira : estoqueSeco;
        int disponivel = origem.getOrDefault(ingrediente, 0);
        
        if (disponivel >= quantidade) {
            origem.put(ingrediente, disponivel - quantidade);
            if (disponivel - quantidade == 0) {
                origem.remove(ingrediente);
            }
            return true;
        }
        return false;
    }
    
    public void desligarEnergia() {
        energiaLigada = false;
        for (Equipamento e : equipamentos) {
            e.desligar();
        }
    }
    
    public void ligarEnergia() {
        energiaLigada = true;
        temperatura = 5.0;
    }
    
    private void deteriorarTudo() {
        for (Ingrediente i : geladeira.keySet()) {
            i.deteriorar();
            i.deteriorar(); // Deteriora duas vezes mais rápido sem energia
        }
    }
    
    private void deteriorarGeladeira() {
        for (Ingrediente i : geladeira.keySet()) {
            i.deteriorar();
        }
    }
    
    private void deteriorarEstoque() {
        for (Ingrediente i : estoqueSeco.keySet()) {
            i.deteriorar();
        }
    }
    
    public Map<Ingrediente, Integer> getGeladeira() {
        return new EnumMap<>(geladeira);
    }
    
    public Map<Ingrediente, Integer> getEstoqueSeco() {
        return new EnumMap<>(estoqueSeco);
    }
    
    public List<Equipamento> getEquipamentos() {
        return new ArrayList<>(equipamentos);
    }
    
    public double getTemperatura() {
        return temperatura;
    }
    
    public boolean isEnergiaLigada() {
        return energiaLigada;
    }
    
    public void fazerManutencaoEquipamento(int index) {
        if (index >= 0 && index < equipamentos.size()) {
            equipamentos.get(index).fazerManutencao();
        }
    }
    
    public void encerrar() {
        if (timerDeterioracao != null) {
            timerDeterioracao.cancel();
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== GELADEIRA ").append(energiaLigada ? "❄️" : "⚠️").append(" ===\n");
        sb.append(String.format("Temperatura: %.1f°C\n", temperatura));
        
        for (Map.Entry<Ingrediente, Integer> entry : geladeira.entrySet()) {
            sb.append(String.format("%s x%d (%s)\n", 
                entry.getKey().getIcone(), 
                entry.getValue(),
                entry.getKey().getQualidadeDescricao()));
        }
        
        sb.append("\n=== ESTOQUE 📦 ===\n");
        for (Map.Entry<Ingrediente, Integer> entry : estoqueSeco.entrySet()) {
            sb.append(String.format("%s x%d (%s)\n", 
                entry.getKey().getIcone(), 
                entry.getValue(),
                entry.getKey().getQualidadeDescricao()));
        }
        
        sb.append("\n=== EQUIPAMENTOS 🔧 ===\n");
        for (Equipamento e : equipamentos) {
            sb.append(e.toString()).append("\n");
        }
        
        return sb.toString();
    }
} 