package entities;

public class Utensilio implements SoltarRecurso {

    private String nome;
    private Cozinha.Chef dono;

    // Getters e Setters --------------------------------------
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Cozinha.Chef getDono() {
        return dono;
    }

    public void setDono(Cozinha.Chef dono) {
        this.dono = dono;
        dono.setRecursoSolicitado(this);
    }

    // Getters e Setters --------------------------------------

    public Utensilio(String nome) {
        this.setNome(nome);
    }

    @Override
    public void release() {

        System.out.println("Porra");

    }
}
