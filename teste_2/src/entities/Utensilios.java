package entities;


import java.util.concurrent.Semaphore;

public class Utensilios {
	
	static Semaphore facas = new Semaphore(1);

	/* static Callable<Boolean> pegarFacas = () ->  {
		return facas.tryAcquire(); 
	}; */
	
	public static class Cozinheiros implements Runnable{
	
		private String nome;
		private Receitas receitaAtribuida;
		private Itens[] inventario;
		
		@Override
		public void run() {
			try {
				
				System.out.println(this.getNome() + " está esperando uma faca...");
				
				if (facas.tryAcquire()) {
					
					System.out.println(this.getNome() + " pegou uma faca");
					
				} else {
				
					Thread.sleep(3000);
					throw new InterruptedException(this.getNome() + 
							" não conseguiu pegar uma faca, pois " +
							"algum cozinheiro já está usando a faca...");
				}
				
			} catch (InterruptedException e) {
				
				System.out.println(e.getMessage());
				
			}
			
		}
		
		public Cozinheiros(String nome) {
			setNome(nome);
			this.inventario = new Itens[14];
		}
	
		public String getNome() {
			return nome;
		}
		
		public Receitas getReceitaAtribuida() {
			return receitaAtribuida;
		}
	
		public void setNome(String nome) {
			this.nome = nome;
		}
		
		public void setReceitaAtribuida(Receitas receita) {
			receitaAtribuida = receita;
			receita.setChefResponsavel(this);
		}
		
		public void CozinharReceita() {
			
			if (receitaAtribuida.PodeSerFeita(inventario)) {
				
				
				
				
				
			}
		}
		
		

	
	}
	
}
