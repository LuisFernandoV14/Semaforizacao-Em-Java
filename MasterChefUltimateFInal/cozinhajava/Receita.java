import java.util.*;

public class Receita {
    private final String nome;
    private final Map<Ingrediente, Integer> ingredientes;
    private final List<Utensilio> utensilios;
    private final List<String> equipamentosNecessarios;
    private final int dificuldade;
    private final int tempoPreparo;
    private final int pontos;
    private final String descricao;
    private final List<String> passos;

    public Receita(String nome, List<Utensilio> utensilios) {
        this.nome = nome;
        this.ingredientes = new EnumMap<>(Ingrediente.class);
        this.utensilios = new ArrayList<>(utensilios);
        this.equipamentosNecessarios = new ArrayList<>();
        this.dificuldade = 1;
        this.tempoPreparo = 10;
        this.pontos = 50;
        this.descricao = "Receita básica";
        this.passos = new ArrayList<>();
    }

    public Receita(String nome, Map<Ingrediente, Integer> ingredientes, List<Utensilio> utensilios, 
                  List<String> equipamentos, int dificuldade, int tempoPreparo, int pontos, 
                  String descricao, List<String> passos) {
        this.nome = nome;
        this.ingredientes = new EnumMap<>(ingredientes);
        this.utensilios = new ArrayList<>(utensilios);
        this.equipamentosNecessarios = new ArrayList<>(equipamentos);
        this.dificuldade = dificuldade;
        this.tempoPreparo = tempoPreparo;
        this.pontos = pontos;
        this.descricao = descricao;
        this.passos = new ArrayList<>(passos);
    }

    public String getNome() {
        return nome;
    }

    public Map<Ingrediente, Integer> getIngredientes() {
        return new EnumMap<>(ingredientes);
    }

    public List<Utensilio> getUtensilios() {
        return new ArrayList<>(utensilios);
    }

    public List<String> getEquipamentosNecessarios() {
        return new ArrayList<>(equipamentosNecessarios);
    }

    public int getDificuldade() {
        return dificuldade;
    }

    public int getTempoPreparo() {
        return tempoPreparo;
    }

    public int getPontos() {
        return pontos;
    }

    public String getDescricao() {
        return descricao;
    }

    public List<String> getPassos() {
        return new ArrayList<>(passos);
    }

    public boolean verificarIngredientes(Estoque estoque) {
        for (Map.Entry<Ingrediente, Integer> entry : ingredientes.entrySet()) {
            Ingrediente ingrediente = entry.getKey();
            int quantidadeNecessaria = entry.getValue();
            
            Map<Ingrediente, Integer> origem = ingrediente.isRefrigerado() ? 
                estoque.getGeladeira() : estoque.getEstoqueSeco();
                
            int disponivel = origem.getOrDefault(ingrediente, 0);
            
            if (disponivel < quantidadeNecessaria || ingrediente.estaEstragado()) {
                return false;
            }
        }
        return true;
    }

    public boolean verificarEquipamentos(List<Equipamento> equipamentos) {
        Set<String> equipamentosDisponiveis = new HashSet<>();
        for (Equipamento e : equipamentos) {
            if (!e.isPrecisaManutencao()) {
                equipamentosDisponiveis.add(e.getNome());
            }
        }
        return equipamentosDisponiveis.containsAll(equipamentosNecessarios);
    }

    public double calcularQualidade(Estoque estoque) {
        if (!verificarIngredientes(estoque)) return 0;
        
        double qualidadeTotal = 0;
        int numIngredientes = 0;
        
        for (Ingrediente i : ingredientes.keySet()) {
            qualidadeTotal += i.getQualidade();
            numIngredientes++;
        }
        
        return qualidadeTotal / numIngredientes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(nome);
        sb.append("\n").append("⭐".repeat(dificuldade));
        sb.append("\n").append(descricao);
        sb.append("\n\nIngredientes:\n");
        
        for (Map.Entry<Ingrediente, Integer> entry : ingredientes.entrySet()) {
            sb.append(String.format("%s %s x%d\n", 
                entry.getKey().getIcone(), 
                entry.getKey().name(), 
                entry.getValue()));
        }
        
        sb.append("\nUtensílios:\n");
        for (Utensilio u : utensilios) {
            sb.append("- ").append(u).append("\n");
        }
        
        sb.append("\nEquipamentos:\n");
        for (String e : equipamentosNecessarios) {
            sb.append("- ").append(e).append("\n");
        }
        
        sb.append("\nModo de Preparo:\n");
        for (int i = 0; i < passos.size(); i++) {
            sb.append(i + 1).append(". ").append(passos.get(i)).append("\n");
        }
        
        sb.append(String.format("\nTempo de Preparo: %d minutos", tempoPreparo));
        sb.append(String.format("\nPontos: %d", pontos));
        
        return sb.toString();
    }
}
