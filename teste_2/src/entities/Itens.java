package entities;

public class Itens {
	
	private String nome;
	private String uso;
	
	
	public String getNome() {
		return nome;
	}
	public String getUso() {
		return uso;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public void setUso(String uso) {
		this.uso = uso;
	}
	
	public Itens(String nome, String uso) {
		this.setNome(nome);
		this.setUso(uso);
	}
	
	

}
