package application;

import entities.*;
import entities.Utensilios.Cozinheiros;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors; 

public class Main {

	public static void main(String[] args) {

		
		Utensilios.Cozinheiros C1 = new Cozinheiros("Luís Fê");
		Utensilios.Cozinheiros C2 = new Cozinheiros("Vítor");
		
		//ExecutorService executor = Executors.newFixedThreadPool(2);
		
		//Thread thread_1 = new Thread(C1);
		//Thread thread_2 = new Thread(C2);
		
		//thread_1.start();
		//thread_2.start();
		
		//executor.execute(C1);
		//executor.execute(C2);
		
		Receitas SopaDeTomate = new Receitas(
				"Sopa de tomate", 
				10000, 
				new String[] {"tomate", "faca", "panela"},
				2);
		
		C1.setReceitaAtribuida(SopaDeTomate);
		
		System.out.println(SopaDeTomate.getChefResponsavel());
		};
	
	
	
//public Receitas(String nome, int tempoParaSerFeita_emMs, String[] itensParaSerFeito, int qtdDeItensParaSerFeito) {


}


