import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Cozinha {
    private final Map<Utensilio, Lock> utensilios;
    private final Map<Utensilio, Chef> utensiliosEmUso;
    private final List<Chef> chefs;
    private final Lock cozinhaLock;
    private Dia diaAtual;
    private static final int LIMITE_PRATOS_SIMULTANEOS = 10;
    private int pratosEmPreparo;
    private final Queue<Chef> filaEspera;
    private List<Pedido> pedidosAtivos = new ArrayList<>();

    public Cozinha() {
        this.utensilios = new HashMap<>();
        this.utensiliosEmUso = new HashMap<>();
        this.chefs = new ArrayList<>();
        this.cozinhaLock = new ReentrantLock();
        this.pratosEmPreparo = 0;
        this.filaEspera = new LinkedList<>();

        // Inicializa os locks para cada utensílio
        for (Utensilio u : Utensilio.values()) {
            utensilios.put(u, new ReentrantLock());
        }
    }

    public void adicionarChef(String nome, Receita receita) {
        if (diaAtual == null) {
            diaAtual = new Dia();
        }
        cozinhaLock.lock();
        try {
            Chef chef = new Chef(nome, receita, this);
            chefs.add(chef);
        } finally {
            cozinhaLock.unlock();
        }
    }

    public boolean podePrepararMaisPratos() {
        cozinhaLock.lock();
        try {
            if (pratosEmPreparo < LIMITE_PRATOS_SIMULTANEOS) {
                pratosEmPreparo++;
                return true;
            }
            return false;
        } finally {
            cozinhaLock.unlock();
        }
    }

    public void finalizarPrato(Chef chef) {
        cozinhaLock.lock();
        try {
            pratosEmPreparo--;
            // Verifica se há chefs esperando na fila
            Chef proximo = filaEspera.poll();
            if (proximo != null) {
                pratosEmPreparo++;
            }
        } finally {
            cozinhaLock.unlock();
        }
    }

    public void encerrarCozinha() {
        cozinhaLock.lock();
        try {
            for (Chef chef : chefs) {
                chef.pararAtividade();
            }
            chefs.clear();
            utensiliosEmUso.clear();
            pratosEmPreparo = 0;
            filaEspera.clear();
        } finally {
            cozinhaLock.unlock();
        }
    }

    public boolean tentarPegarUtensilio(Chef chef, Utensilio utensilio) {
        // Usa a estratégia de tryLock para evitar bloqueios
        Lock lock = utensilios.get(utensilio);
        if (lock.tryLock()) {
            try {
                cozinhaLock.lock();
                try {
                    if (utensiliosEmUso.containsKey(utensilio)) {
                        // Se já estiver em uso, libera o lock e adiciona chef à fila
                        lock.unlock();
                        filaEspera.offer(chef);
                        return false;
                    }
                    // Registra o utensílio como em uso por este chef
                    utensiliosEmUso.put(utensilio, chef);
                    return true;
                } finally {
                    cozinhaLock.unlock();
                }
            } catch (Exception e) {
                // Em caso de erro, garante que o lock é liberado
                lock.unlock();
                System.err.println("Erro ao tentar pegar utensílio: " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        // Se não conseguiu o lock, adiciona à fila de espera
        filaEspera.offer(chef);
        return false;
    }

    public void liberarUtensilio(Utensilio utensilio) {
        cozinhaLock.lock();
        try {
            // Remove o chef que estava usando este utensílio
            Chef chef = utensiliosEmUso.remove(utensilio);
            if (chef != null) {
                try {
                    // Libera o lock do utensílio
                    utensilios.get(utensilio).unlock();
                } catch (IllegalMonitorStateException e) {
                    // Ignora o erro se o lock já foi liberado
                    System.out.println("Aviso: Lock do utensílio " + utensilio + " já liberado.");
                }
                
                // Verifica se há chefs esperando por este utensílio
                if (!filaEspera.isEmpty()) {
                    Chef proximo = filaEspera.poll();
                    if (proximo != null && proximo.getStatus() == Chef.Status.ESPERANDO) {
                        // Tenta dar o utensílio para o próximo chef na fila
                        if (tentarPegarUtensilio(proximo, utensilio)) {
                            System.out.println("Utensílio " + utensilio + " transferido para " + proximo.getNome());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao liberar utensílio: " + e.getMessage());
            e.printStackTrace();
        } finally {
            cozinhaLock.unlock();
        }
    }

    public List<Chef> getChefs() {
        cozinhaLock.lock();
        try {
            return new ArrayList<>(chefs);
        } finally {
            cozinhaLock.unlock();
        }
    }

    public Chef getChefComUtensilio(Utensilio utensilio) {
        cozinhaLock.lock();
        try {
            return utensiliosEmUso.get(utensilio);
        } finally {
            cozinhaLock.unlock();
        }
    }

    public int getPratosEmPreparo() {
        cozinhaLock.lock();
        try {
            return pratosEmPreparo;
        } finally {
            cozinhaLock.unlock();
        }
    }

    public int getTamanhoFilaEspera() {
        cozinhaLock.lock();
        try {
            return filaEspera.size();
        } finally {
            cozinhaLock.unlock();
        }
    }

    // Método para verificar se um utensílio está disponível para uso
    public boolean checarUtensilioDisponivel(Utensilio utensilio) {
        return !utensiliosEmUso.containsKey(utensilio);
    }
    
    // Método para reservar um utensílio para um chef
    public boolean reservarUtensilio(Utensilio utensilio, Chef chef) {
        cozinhaLock.lock();
        try {
            if (checarUtensilioDisponivel(utensilio)) {
                Lock lock = utensilios.get(utensilio);
                if (lock.tryLock()) {
                    utensiliosEmUso.put(utensilio, chef);
                    System.out.println(" Utensílio " + utensilio + " reservado para " + chef.getNome());
                    return true;
                } else {
                    System.out.println(" Não conseguiu obter lock para " + utensilio);
                    return false;
                }
            }
            return false;
        } finally {
            cozinhaLock.unlock();
        }
    }
    
    public void adicionarPedido(Pedido pedido) {
        pedidosAtivos.add(pedido);
    }
    
    public List<Pedido> getPedidosAtivos() {
        return new ArrayList<>(pedidosAtivos);
    }
}
