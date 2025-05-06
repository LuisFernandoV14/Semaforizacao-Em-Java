package entities;

public class Receitas {

	private String nome;
	private int tempoParaSerFeita_emMs;
	public String[] itensParaSerFeito;
	private int qtdDeItensParaSerFeito;
	private Utensilios.Cozinheiros chefResponsavel;
	
	
	public Receitas(String nome, int tempoParaSerFeita_emMs, String[] itensParaSerFeito, int qtdDeItensParaSerFeito) {
		super();
		this.nome = nome;
		this.tempoParaSerFeita_emMs = tempoParaSerFeita_emMs;
		this.itensParaSerFeito = itensParaSerFeito;
		this.qtdDeItensParaSerFeito = qtdDeItensParaSerFeito;
		
	}

	
	// Getter e Setters ---------------------------------------------------------
	
	public String getNome() {
		return nome;
	}

	public int getTempoParaSerFeita_emMs() {
		return tempoParaSerFeita_emMs;
	}
	
	public String[] getItensParaSerFeito() {
		return itensParaSerFeito;
	}

	public int getQtdDeItensParaSerFeito() {
		return qtdDeItensParaSerFeito;
	}
	
	public String getChefResponsavel() {
		return chefResponsavel.getNome();
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setTempoParaSerFeita_emMs(int tempoParaSerFeita_emMs) {
		this.tempoParaSerFeita_emMs = tempoParaSerFeita_emMs;
	}

	public void setItensParaSerFeito(String[] itensParaSerFeito) {
		this.itensParaSerFeito = itensParaSerFeito;
	}

	public void setQtdDeItensParaSerFeito(int qtdDeItensParaSerFeito) {
		this.qtdDeItensParaSerFeito = qtdDeItensParaSerFeito;
	}
	
	public void setChefResponsavel(Utensilios.Cozinheiros chefResponsavel) {
		this.chefResponsavel = chefResponsavel;
	}
	// Getter e Setters ---------------------------------------------------------

	public boolean PodeSerFeita(Itens[] inventario){
		
		int i, j = 0;
		for(i = 0; i < this.getQtdDeItensParaSerFeito(); i++) {
			
			if (inventario[i].getNome() == this.itensParaSerFeito[i]) { j++; } 
			else {
			
				System.out.println(this.getNome() + " não pode ser feita. " + 
									this.chefResponsavel.getNome() + " não possui " +
									itensParaSerFeito[i] +  " o suficiente.");
				return false;
			
			}
			
		}
		
		if (j == this.getQtdDeItensParaSerFeito()) return true;
		
		return false;
		
		
	}
	
}

