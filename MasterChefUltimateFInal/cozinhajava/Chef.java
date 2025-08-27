import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Chef implements Runnable {
    public enum Status {
        LIVRE("Livre", "✅"),
        PREPARANDO("Preparando", ""),
        ESPERANDO("Esperando Utensílio", ""),
        FINALIZANDO("Finalizando", "");
        
        private final String descricao;
        private final String icone;

        Status(String descricao, String icone) {
            this.descricao = descricao;
            this.icone = icone;
        }
        
        public String getDescricao() {
            return descricao;
        }

        public String getIcone() {
            return icone;
        }
    }

    public enum Habilidade {
        RAPIDEZ_PANELA("Especialista em Panela", 1.5, 1.0),
        SOBREMESAS("Mestre em Sobremesas", 1.2, 1.3),
        RAPIDEZ_FORNO("Especialista em Forno", 1.3, 1.2),
        MULTITAREFA("Multitarefas", 1.1, 1.4),
        PRECISAO("Precisão na Execução", 1.0, 1.5);
        
        private final String descricao;
        private final double fatorVelocidade;
        private final double fatorQualidade;
        
        Habilidade(String descricao, double fatorVelocidade, double fatorQualidade) {
            this.descricao = descricao;
            this.fatorVelocidade = fatorVelocidade;
            this.fatorQualidade = fatorQualidade;
        }
        
        public String getDescricao() {
            return descricao;
        }
        
        public double getFatorVelocidade() {
            return fatorVelocidade;
        }
        
        public double getFatorQualidade() {
            return fatorQualidade;
        }
    }

    private final String nome;
    private Status status;
    private final List<Utensilio> utensiliosEmUso;
    private final List<Utensilio> utensiliosNecessarios;
    private Utensilio utensilioAtual;
    private final Lock lock;
    private boolean ativo;
    private Thread thread;
    private final Cozinha cozinha;
    private Receita receitaAtual;
    private int experiencia;
    private double eficiencia;
    private int progressoAtual;
    private int tempoTotalPreparo;
    private long inicioPreparacao;
    private Runnable aoFinalizar;
    private final Habilidade habilidade;

    public Chef(String nome, Receita receita, Cozinha cozinha) {
        this.nome = nome;
        this.status = Status.LIVRE;
        this.utensiliosEmUso = new ArrayList<>();
        this.utensiliosNecessarios = new ArrayList<>(receita.getUtensilios());
        this.lock = new ReentrantLock();
        this.ativo = true;
        this.cozinha = cozinha;
        this.receitaAtual = receita;
        this.experiencia = 0;
        this.habilidade = Habilidade.values()[new Random().nextInt(Habilidade.values().length)];
        this.eficiencia = (habilidade.getFatorVelocidade() + habilidade.getFatorQualidade()) / 2;
        this.progressoAtual = 0;
        this.tempoTotalPreparo = receita.getTempoPreparo() * 1000;
        this.thread = new Thread(this);
        this.thread.start();

    }

    public Chef(String nome, Habilidade habilidade, Cozinha cozinha) {
        this.nome = nome;
        this.status = Status.LIVRE;
        this.utensiliosEmUso = new ArrayList<>();
        this.utensiliosNecessarios = new ArrayList<>();
        this.lock = new ReentrantLock();
        this.ativo = true;
        this.cozinha = cozinha;
        this.receitaAtual = null;
        this.experiencia = 0;
        this.eficiencia = (habilidade.getFatorVelocidade() + habilidade.getFatorQualidade()) / 2;
        this.progressoAtual = 0;
        this.tempoTotalPreparo = 0;
        this.habilidade = habilidade;
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run() {
        while (ativo) {
            try {
                // Se o chef está LIVRE e NÃO TEM UMA RECEITA ATUAL designada pelo JogoCozinha,
                // ele pode estar em um estado "ocioso" ou aguardando.
                // Se ele TEM uma receitaAtual, significa que JogoCozinha está gerenciando seu preparo.
                // Neste caso, a thread do Chef não deve fazer nada para não interferir.
                if (status == Status.LIVRE && receitaAtual == null) {
                    // Chef está verdadeiramente ocioso.
                    Thread.sleep(1000); // Dorme um pouco e verifica novamente.
                } else if (status == Status.ESPERANDO && receitaAtual != null) {
                    // Se JogoCozinha o colocou como ESPERANDO, esta thread não deve mudar isso para LIVRE.
                    // JogoCozinha (via progressoTimer) tentará obter os utensílios.
                    Thread.sleep(500); // Apenas dorme e permite que JogoCozinha trabalhe.
                } else if ((status == Status.PREPARANDO || status == Status.FINALIZANDO) && receitaAtual != null) {
                    // Se JogoCozinha está gerenciando o preparo/finalização, esta thread não interfere.
                     Thread.sleep(500); // Apenas dorme. O progresso é via JogoCozinha.
                } else {
                    // Outros casos ou status inesperados, apenas dorme.
                    // Ou se status for LIVRE mas TEM receitaAtual (estado transitório antes do progressoTimer assumir).
                    Thread.sleep(500);
                }
                // A atualização da interface agora é mais impulsionada por JogoCozinha
                // e pelos setters de status no Chef.
                // atualizarInterface(); // Pode ser redundante se JogoCozinha já atualiza.

                if (progressoAtual == 100) {
                    this.receitaAtual = null;
                    atualizarInterface();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(" Thread do Chef " + nome + " interrompida.");
                ativo = false; // Garante que o loop termine.
                break;
            } catch (Exception e) {
                System.err.println("[ERRO] Exceção na thread do Chef " + nome + ": " + e.getMessage());
                e.printStackTrace();
                // Considerar uma pausa maior para evitar spam de erros.
                try { Thread.sleep(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt();}
            }
        }
        System.out.println(" Thread do Chef " + nome + " terminando.");
    }

    // Comentado pois o progresso é controlado externamente por JogoCozinha via atualizarProgresso(int)
    /*
    private void atualizarProgresso() {
        if (status == Status.PREPARANDO) {
            long tempoDecorrido = System.currentTimeMillis() - inicioPreparacao;
            progressoAtual = (int) ((tempoDecorrido * 100) / tempoTotalPreparo);
            progressoAtual = Math.min(progressoAtual, 100);
        }
        atualizarInterface();
    }
    */

    private void finalizarPreparo() {
        try {
            // Força o progresso para 100% para garantir visualização correta
            progressoAtual = 100;
            atualizarInterface();
            // Libera todos os utensílios
            List<Utensilio> utensiliosCopia = new ArrayList<>(utensiliosEmUso);
            for (Utensilio u : utensiliosCopia) {
                liberarUtensilio(u);
                System.out.println(" Chef " + nome + " liberou utensílio: " + u);   
                this.receitaAtual = null;
                receitaAtual = null;
            }
            
            // Notifica a cozinha que o prato foi finalizado
            cozinha.finalizarPrato(this);
            
            // Executa o callback de finalização, se existir
            if (aoFinalizar != null) {
                System.out.println(" Chef " + nome + " executando callback de finalização");
                try {
                    aoFinalizar.run();
                } catch (Exception e) {
                    System.err.println("[ERRO] Falha ao executar callback de finalização: " + e.getMessage());
                    e.printStackTrace();
                }
                aoFinalizar = null; // Limpa para evitar execuções duplicadas
            } else {
                System.out.println(" Chef " + nome + " não tem callback de finalização definido");
            }
            
            // Reseta o progresso e atualiza a interface
            progressoAtual = 0;
            atualizarInterface();
            
            // Garante que o chef volta a LIVRE
            status = Status.LIVRE;
        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao finalizar preparo: " + e.getMessage());
            e.printStackTrace();
            
            // Mesmo com erro, garante que o chef volte ao estado LIVRE
            status = Status.LIVRE;
            progressoAtual = 0;
        }
    }

    public void pausarAtividade() {
        this.status = Status.ESPERANDO;
        atualizarInterface();
    }

    public void liberarUtensilio(Utensilio utensilio) {
        if (utensiliosEmUso.remove(utensilio)) {
            cozinha.liberarUtensilio(utensilio);
            pausarAtividade();
        }
        if (this.status == Status.ESPERANDO && utensiliosEmUso.isEmpty()) {
            this.status = Status.LIVRE;
        }
       
        atualizarInterface();
    }

    public void pararAtividade() {
        ativo = false;
        thread.interrupt();
        new ArrayList<>(utensiliosEmUso).forEach(this::liberarUtensilio);
    }

    public String getNome() {
        return nome;
    }

    public Status getStatus() {
        return status;
    }

    public List<Utensilio> getUtensiliosEmUso() {
        return new ArrayList<>(utensiliosEmUso);
    }

    public Utensilio getUtensilioAtual() {
        return utensilioAtual;
    }

    public Lock getLock() {
        return lock;
    }

    public Receita getReceitaAtual() {
        return receitaAtual;
    }

    public int getExperiencia() {
        return experiencia;
    }

    public double getEficiencia() {
        return eficiencia;
    }

    public int getProgressoAtual() {
        return progressoAtual;
    }

    public void setReceitaAtual(Receita receita) {
        this.receitaAtual = receita;
        this.utensiliosNecessarios.clear();
        this.utensiliosNecessarios.addAll(receita.getUtensilios());
        this.tempoTotalPreparo = receita.getTempoPreparo() * 1000;
        this.progressoAtual = 0;
        atualizarInterface();
    }

    public void setAoFinalizar(Runnable callback) {
        this.aoFinalizar = callback;
        atualizarInterface();
    }

    public Habilidade getHabilidade() { return habilidade; }

    public void setStatus(Status status) {
        this.status = status;
        atualizarInterface();
    }

    public void adicionarUtensilioObtido(Utensilio u) {
        if (!this.utensiliosEmUso.contains(u)) {
            this.utensiliosEmUso.add(u);
            // Opcional: log ou atualizar interface específica para quando um utensílio é pego
            System.out.println(" Chef " + this.nome + " adicionou utensílio: " + u);
            atualizarInterface(); 
        }
    }

    private void atualizarInterface() {
        SwingUtilities.invokeLater(() -> {
            JogoCozinha.atualizarPainelCozinhaStatic();
            JogoCozinha.atualizarPainelPedidosStatic();
        });
    }

    public boolean usarUtensilio(Utensilio utensilio) throws Exception {
        if (status == Status.LIVRE || status == Status.PREPARANDO) {
            if (cozinha.reservarUtensilio(utensilio, this)) {
                utensiliosEmUso.add(utensilio);
                return true;
            } else {
                throw new Exception("Utensílio " + utensilio + " não está disponível");
            }
        }
        return false;
    }

    public void aoFinalizar() {
        try {
            if (aoFinalizar != null) {
                System.out.println(" Executando callback de finalização para o chef " + nome);
                aoFinalizar.run();
                // Limpa o callback para evitar execuções duplicadas
                aoFinalizar = null;
            } else {
                System.out.println(" Callback de finalização não definido para o chef " + nome);
                atualizarInterface();
            }
        } catch (Exception e) {
            System.err.println("Erro ao executar callback de finalização: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void iniciarProgresso() {
        this.progressoAtual = 0;
    }

    public void atualizarProgresso(int novoProgresso) {
        this.progressoAtual = Math.min(100, Math.max(0, novoProgresso));
        // Notifica para atualizar interface
        JogoCozinha.atualizarPainelCozinhaStatic();
    }
}
