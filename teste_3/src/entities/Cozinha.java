package entities;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Cozinha {

    static Random aleatorio = new Random();
    static Semaphore faca = new Semaphore(1);

    public static class Chef implements Runnable{

        private String nome;
        private Utensilio recursoSolicitado;
        private Utensilio[] inventario;
        private int nmrInv;

        public Chef(String nome) {

            setNome(nome);
            setInventario();
            setNmrInv(0);

        }

        @Override
        public void run() {
            try {

                switch (recursoSolicitado.getNome()) {
                    case "faca":
                        if(faca.tryAcquire()) {

                            int tempoMinimoFacas = 2; // Essa variável simula o tempo mínimo, em segundos, que um cozinheiro abre uma gaveta e pega uma faca na vida real.
                            Thread.sleep((aleatorio.nextInt(4) + tempoMinimoFacas) * 1000); // Gera um número aleatório entre 0 e 3 (bound 4), adiciona o tempo mínimo e converte em milissegundos.

                            adicionarNoInventario(new Utensilio("faca"));

                        }
                    break;

                }


            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                return;
            } catch (UnsupportedOperationException e) {
                System.out.println(e.getMessage());

            }
        }

        // Getters e Setters --------------------------------------
        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public Utensilio getRecursoSolicitado() {
            return recursoSolicitado;
        }

        public void setRecursoSolicitado(Utensilio recursoSolicitado) {
            this.recursoSolicitado = recursoSolicitado;
        }

        public Utensilio[] getInventario() {
            return inventario;
        }

        public void setInventario() {
            this.inventario = new Utensilio[5];
        }

        public int getNmrInv() {
            return nmrInv;
        }

        public void setNmrInv(int nmrInv) {
            this.nmrInv = nmrInv;
        }

        // Getters e Setters --------------------------------------

        public void adicionarNoInventario(Utensilio item) throws UnsupportedOperationException{

            if (getNmrInv() >= 5) {
                throw new UnsupportedOperationException("O chef " + getNome() + " não pôde pegar um(a)" + getRecursoSolicitado().getNome() + " porque seu inventário está cheio");
            }

            inventario[nmrInv] = item;
            setNmrInv(nmrInv + 1);
            System.out.println("O chef " + getNome() + " pegou um(a) " + getRecursoSolicitado().getNome() );

        }

        public void mostrarInventario() {

            System.out.println("Utensílios no inventário do chef: " + nome + ".");

            for(int i = 0; i < nmrInv; i++) {

                System.out.println("- Utensilio na posição " + (i + 1) + ": " + inventario[i].getNome() + ".");

            }

            System.out.print("\n");

        }


    }


}
