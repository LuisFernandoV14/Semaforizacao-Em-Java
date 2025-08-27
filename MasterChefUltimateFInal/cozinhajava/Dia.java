import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Dia {
    private final Cozinha cozinha;
    private final List<Chef> chefs;
    private boolean modoGrafico;

    public Dia() {
        this(true);  // Por padrão, usa modo gráfico
    }

    public Dia(boolean modoGrafico) {
        this.modoGrafico = modoGrafico;
        this.cozinha = new Cozinha();
        this.chefs = new ArrayList<>();
    }

    public void adicionarChef(String nome, Receita receita) {
        if (modoGrafico) {
            cozinha.adicionarChef(nome, receita);
        } else {
            Chef chef = new Chef(nome, receita, cozinha);
            chefs.add(chef);
        }
    }

    public void encerrarDia() {
        if (modoGrafico) {
            cozinha.encerrarCozinha();
        } else {
            for (Chef chef : chefs) {
                chef.pararAtividade();
            }
        }
    }

    public void liberarUtensilio(Utensilio u) {
        if (modoGrafico) {
            cozinha.liberarUtensilio(u);
        }
    }

    public List<Chef> getChefs() {
        return modoGrafico ? cozinha.getChefs() : chefs;
    }

    public Cozinha getCozinha() {
        return cozinha;
    }
}
