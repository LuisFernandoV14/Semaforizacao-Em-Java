import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;


public class JogoCozinha {
    // Cores do tema
    private static Color COR_PRIMARIA = new Color(0, 0, 185);
    private static Color COR_SECUNDARIA = new Color(46, 204, 113);
    private static Color COR_DESTAQUE = new Color(231, 76, 60);
    private static Color COR_FUNDO = new Color(236, 240, 241);
    private static Color COR_TEXTO = new Color(0, 0, 0);
    private static Color COR_PANEL = new Color(255,255,255,240);
    private static Color COR_LOG_BG = new Color(0,0,0);
    private static Color COR_LOG_TXT = new Color(52, 73, 94);
    
    
    private final Cozinha cozinha;
    private final JFrame frame;
    private final JPanel mainPanel;
    private final JTextArea logArea;
    private final JComboBox<String> receitaCombo;
    private final JPanel cozinhaPanel;
    private int pontuacao = 0;
    private int nivel = 1;
    private final JLabel pontuacaoLabel;
    private final JLabel nivelLabel;
    private final Timer timer;
    private int segundosRestantes = 300;
    private final JLabel timerLabel;
    private final JPanel eventosPanel;
    private final JProgressBar progressBar;
    private final Random random = new Random();
    private JLayeredPane cozinhaLayeredPane;
    private Timer atualizacaoTimer;
    private Map<Chef, ChefSprite> chefSprites = new HashMap<>();
    private JPanel utensiliosPanel = new JPanel();
    private Timer pedidoAutoTimer;

    private static final String[] RECEITAS = {
        "🍳 Omelete Especial", 
        "🥤 Super Vitamina", 
        "🥘 Sopa do Chef", 
        "🎂 Bolo de Chocolate", 
        "🍝 Lasanha Supreme", 
        "🥗 Salada Caesar", 
        "🍚 Risoto Gourmet",
        "🌮 Tacos Mexicanos",
        "🍕 Pizza Artesanal",
        "🍜 Lámen Tradicional"
    };

    private static final String[] EVENTOS = {
        " EMERGÊNCIA: Fogo na cozinha! Libere a panela AGORA! (+30 pontos se liberar em 3s)",
        " ALERTA: Queda de energia! Todos os equipamentos precisam ser reiniciados! (+25 pontos)",
        " ESPECIAL: Cliente VIP chegou! Prepare algo elaborado! (+40 pontos por receita complexa)",
        " DESAFIO: Festival Gourmet! Bônus de pontos por pratos especiais! (+50 pontos)",
        " CORRERIA: Hora do Rush! Prepare 3 pratos em 1 minuto! (+60 pontos)",
        " PRECISÃO: Masterchef na cozinha! Seja perfeito nas próximas ações! (+45 pontos)"
    };

    private static class Pontuacao implements Comparable<Pontuacao> {
        private final String nomeJogador;
        private final int pontos;
        private final String data;

        public Pontuacao(String nomeJogador, int pontos) {
            this.nomeJogador = nomeJogador;
            this.pontos = pontos;
            this.data = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        @Override
        public int compareTo(Pontuacao outra) {
            return Integer.compare(outra.pontos, this.pontos); // Ordem decrescente
        }

        @Override
        public String toString() {
            return String.format("%s - %d pontos (%s)", nomeJogador, pontos, data);
        }
    }

    private List<Pontuacao> melhoresPontuacoes = new ArrayList<>();
    private static final String ARQUIVO_PONTUACOES = "pontuacoes.txt";

    private static class Conquista {
        private final String nome;
        private final String descricao;
        private final String icone;
        private boolean desbloqueada;
        private final int pontosNecessarios;

        public Conquista(String nome, String descricao, String icone, int pontosNecessarios) {
            this.nome = nome;
            this.descricao = descricao;
            this.icone = icone;
            this.pontosNecessarios = pontosNecessarios;
            this.desbloqueada = false;
        }

        public boolean verificar(int pontuacaoAtual) {
            if (!desbloqueada && pontuacaoAtual >= pontosNecessarios) {
                desbloqueada = true;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("%s %s - %s", icone, nome, descricao);
        }
    }

    private List<Conquista> conquistas = new ArrayList<>();
    private List<String> dicas = new ArrayList<>();
    private Timer dicasTimer;

    private static class EventoEspecial {
        private final String nome;
        private final String descricao;
        private final String icone;
        private final int duracao; // segundos
        private final int bonusPontos;
        private int tempoRestante;
        private boolean ativo;

        public EventoEspecial(String nome, String descricao, String icone, int duracao, int bonusPontos) {
            this.nome = nome;
            this.descricao = descricao;
            this.icone = icone;
            this.duracao = duracao;
            this.bonusPontos = bonusPontos;
            this.ativo = false;
            this.tempoRestante = duracao;
        }

        public void ativar() {
            ativo = true;
            tempoRestante = duracao;
        }

        public boolean atualizar() {
            if (ativo && tempoRestante > 0) {
                tempoRestante--;
                return true;
            }
            if (tempoRestante <= 0) {
                ativo = false;
            }
            return false;
        }

            @Override
        public String toString() {
            return String.format("%s %s (%ds)\n%s", icone, nome, tempoRestante, descricao);
        }
    }

    private List<EventoEspecial> eventosDisponiveis = new ArrayList<>();
    private List<EventoEspecial> eventosAtivos = new ArrayList<>();
    private Timer eventoTimer;

    private int totalReceitasPreparadas = 0;
    private int totalEventosParticipados = 0;
    private int maiorPontuacaoReceita = 0;
    private String receitaMaisPontos = "";

    private List<Pedido> pedidosAtivos = new ArrayList<>();
    private JPanel pedidosPanel;

    private static class ChefSprite extends JPanel {
        private final Chef chef;
        private final List<Color> stateColors;
        private int currentState = 0;
        private Timer animationTimer;
        private Point position;
        private Point targetPosition;
        private boolean isMoving = false;
        private List<Utensilio> utensiliosVisuais = new ArrayList<>();
        private final Random spriteRandom = new Random();
        private final JogoCozinha jogoCozinha;
        private static final int CHEF_SIZE = 50;

        public ChefSprite(JogoCozinha jogoCozinha, Chef chef, Point initialPosition) {
            this.jogoCozinha = jogoCozinha;
            this.chef = chef;
            this.position = initialPosition;
            this.targetPosition = initialPosition;
            this.stateColors = initializeColors();
            setOpaque(false);
            setPreferredSize(new Dimension(CHEF_SIZE, CHEF_SIZE));
            
            // Timer para animação
            animationTimer = new Timer(150, e -> {
                currentState = (currentState + 1) % stateColors.size();
                if (isMoving) {
                    moverParaAlvo();
                }
                repaint();
            });
            animationTimer.start();
        }

        private List<Color> initializeColors() {
            List<Color> colors = new ArrayList<>();
            // Cores para diferentes estados
            colors.add(new Color(46, 204, 113));  // Verde para LIVRE
            colors.add(new Color(231, 76, 60));   // Vermelho para PREPARANDO
            colors.add(new Color(241, 196, 15));  // Amarelo para ESPERANDO
            return colors;
        }

        public Point getPosition() {
            return position;
        }

        public void setTargetPosition(Point target) {
            this.targetPosition = target;
            this.isMoving = true;
        }

        private void moverParaAlvo() {
            double dx = targetPosition.x - position.x;
            double dy = targetPosition.y - position.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < 5) {
                position = targetPosition;
                isMoving = false;
                return;
            }
            
            double speed = 5.0;
            position.x += (dx / distance) * speed;
            position.y += (dy / distance) * speed;
        }

        public void adicionarUtensilio(Utensilio u) {
            if (!utensiliosVisuais.contains(u)) {
                utensiliosVisuais.add(u);
                repaint();
            }
        }

        public void removerUtensilio(Utensilio u) {
            utensiliosVisuais.remove(u);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Configurações para melhor qualidade
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Desenha o chef como um círculo colorido
            Color chefColor = stateColors.get(currentState);
            g2d.setColor(chefColor);
            g2d.fillOval(position.x, position.y, CHEF_SIZE, CHEF_SIZE);
            
            // Borda do chef
            g2d.setColor(chefColor.darker());
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(position.x, position.y, CHEF_SIZE, CHEF_SIZE);
            
            // Desenha o nome do chef
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();
            String nome = chef.getNome();
            int textWidth = fm.stringWidth(nome);
            g2d.drawString(nome, position.x + CHEF_SIZE/2 - textWidth/2, position.y - 5);
            
            // Desenha os utensílios em uso
            int offsetX = 0;
            for (Utensilio u : utensiliosVisuais) {
                String icone = jogoCozinha.getIconeUtensilio(u);
                g2d.drawString(icone, position.x + offsetX, position.y + CHEF_SIZE + 15);
                offsetX += 20;
            }
            
            // Desenha o status atual
            String statusIcone = chef.getStatus().getIcone();
            g2d.drawString(statusIcone, position.x + CHEF_SIZE + 5, position.y + 15);
            
            // Desenha a barra de progresso se estiver preparando
            if (chef.getStatus() == Chef.Status.PREPARANDO || chef.getStatus() == Chef.Status.FINALIZANDO) {
                int progresso = chef.getProgressoAtual();
                int barraLargura = 80;
                int barraAltura = 8;
                int barraX = position.x - 15;
                int barraY = position.y + CHEF_SIZE + 25;
                
                // Fundo da barra
                g2d.setColor(new Color(200, 200, 200));
                g2d.fillRect(barraX, barraY, barraLargura, barraAltura);
                
                // Progresso
                Color corProgresso = chef.getStatus() == Chef.Status.FINALIZANDO ? 
                    COR_SECUNDARIA : COR_PRIMARIA;
                g2d.setColor(corProgresso);
                g2d.fillRect(barraX, barraY, (int)(barraLargura * progresso / 100.0), barraAltura);
                
                // Borda da barra
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(barraX, barraY, barraLargura, barraAltura);
                
                // Porcentagem
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.drawString(progresso + "%", barraX + barraLargura + 5, barraY + barraAltura);
            }
        }
    }

    private static JogoCozinha instanciaAtual;

    public JogoCozinha() {
        System.out.println(" Iniciando construtor JogoCozinha");
        instanciaAtual = this;
        cozinha = new Cozinha();
        inicializarChefsPreEstabelecidos();
        frame = new JFrame("🎮 Master Chef Ultimate");
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COR_FUNDO);
        
        logArea = new JTextArea(8, 40);
        receitaCombo = new JComboBox<>(RECEITAS);
        cozinhaPanel = new JPanel();
        pontuacaoLabel = new JLabel("0");
        nivelLabel = new JLabel("Nível 1");
        timerLabel = new JLabel("5:00");
        eventosPanel = new JPanel();
        progressBar = new JProgressBar(0, 100);
        
        timer = new Timer(1000, e -> atualizarTempo());
        
        // Inicializar sistemas
        inicializarConquistas();
        inicializarDicas();
        inicializarEventos();
        
        // Timer para mostrar dicas
        dicasTimer = new Timer(30000, e -> mostrarDicaAleatoria());
        dicasTimer.start();
        
        configurarInterface();
        aplicarTema();
        iniciarJogo();
        atualizarPainelPedidos();
        atualizarPainelCozinha();
        atualizarPainelEventos();
        verificarECorrigirLayout();
        System.out.println(" Fim do construtor JogoCozinha");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(true);
        frame.dispose();
        frame.setUndecorated(true);
        frame.setVisible(true);
    }

    private void configurarInterface() {
        System.out.println(" Iniciando configurarInterface para notebook");
        mainPanel.removeAll();
        
        // Configuração para notebook - layout horizontal
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(COR_FUNDO);
        
        // --- CABEÇALHO SUPERIOR ---
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(COR_FUNDO);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Info do jogo
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
            infoPanel.setOpaque(false);
            
        // Pontuação
        JLabel pontuacaoIcon = new JLabel("");
        pontuacaoIcon.setFont(new Font("Arial", Font.PLAIN, 24));
        pontuacaoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        // Nível
        JLabel nivelIcon = new JLabel("");
        nivelIcon.setFont(new Font("Arial", Font.PLAIN, 24));
        nivelLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Timer
        JLabel timerIcon = new JLabel("");
        timerIcon.setFont(new Font("Arial", Font.PLAIN, 24));
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Progresso do nível
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.BOLD, 14));
        progressBar.setPreferredSize(new Dimension(200, 24));
        progressBar.setForeground(COR_SECUNDARIA);
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setBorder(BorderFactory.createLineBorder(Color.GREEN));
        
        infoPanel.add(pontuacaoIcon); infoPanel.add(pontuacaoLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(nivelIcon); infoPanel.add(nivelLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(timerIcon); infoPanel.add(timerLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(progressBar);
        headerPanel.add(infoPanel, BorderLayout.WEST);
        
        // Botões da direita
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botoesPanel.setOpaque(false);
        
        JButton btnConquistas = criarBotaoEstilizado(" Conquistas", COR_PRIMARIA);
        btnConquistas.addActionListener(e -> mostrarConquistas());
        
        JButton btnReceitas = criarBotaoEstilizado(" Receitas", COR_PRIMARIA);
        btnReceitas.addActionListener(e -> mostrarReceitasEUtensilios());
        
        
        
        botoesPanel.add(btnConquistas);
        botoesPanel.add(btnReceitas);
        headerPanel.add(botoesPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // --- PAINEL CENTRAL ---
        JPanel centroPanel = new JPanel(new BorderLayout(10, 10));
        centroPanel.setBackground(COR_FUNDO);
        
        // --- PAINEL DE PEDIDOS (ESQUERDA) ---
        JPanel pedidosContainer = new JPanel(new BorderLayout(0, 10));
        pedidosContainer.setBackground(COR_FUNDO);
        pedidosContainer.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
            "Pedidos", TitledBorder.DEFAULT_JUSTIFICATION, 
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 16), COR_TEXTO));
        
        // Painel de controle de pedidos
        JPanel pedidoControle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pedidoControle.setOpaque(false);
        
        JButton btnNovoPedido = criarBotaoEstilizado(" Novo Pedido", COR_PRIMARIA);
        btnNovoPedido.addActionListener(e -> { 
            gerarPedidoAleatorio(); 
            logArea.append("\n Novo pedido gerado manualmente!\n"); 
        });
        pedidoControle.add(btnNovoPedido);
        
        pedidosContainer.add(pedidoControle, BorderLayout.NORTH);
        
        // Lista de pedidos com scroll
        pedidosPanel = new JPanel();
        pedidosPanel.setLayout(new BoxLayout(pedidosPanel, BoxLayout.Y_AXIS));
        pedidosPanel.setBackground(COR_PANEL);
        JScrollPane pedidosScroll = new JScrollPane(pedidosPanel);
        pedidosScroll.setBorder(null);
        pedidosScroll.getVerticalScrollBar().setUnitIncrement(16);
        pedidosContainer.add(pedidosScroll, BorderLayout.CENTER);
        pedidosContainer.setPreferredSize(new Dimension(350, 0));
        
        // --- PAINEL CENTRAL DE COZINHA ---
        JPanel cozinhaCentroPanel = new JPanel(new BorderLayout(0, 10));
        cozinhaCentroPanel.setBackground(COR_FUNDO);
        
        // Painel de cozinha principal
        cozinhaPanel.setLayout(new BorderLayout());
        cozinhaPanel.setBackground(COR_FUNDO);
        cozinhaPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
            "Cozinha", TitledBorder.DEFAULT_JUSTIFICATION, 
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 16), COR_TEXTO));
        
        /*cozinhaLayeredPane = new JLayeredPane();
        cozinhaLayeredPane.setPreferredSize(new Dimension(800, 400)); // Tamanho para notebook
        cozinhaLayeredPane.setLayout(null);
        cozinhaPanel.setBounds(0, 0, 800, 400);
        cozinhaLayeredPane.add(cozinhaPanel, JLayeredPane.DEFAULT_LAYER);
        cozinhaCentroPanel.add(cozinhaLayeredPane, BorderLayout.CENTER);*/
        
        // Painel de utensílios
        utensiliosPanel = new JPanel();
        utensiliosPanel.setLayout(new GridLayout(2, 5, 10, 10));
                utensiliosPanel.setOpaque(false);
        utensiliosPanel.removeAll();
        utensiliosPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
            "Utensílios", TitledBorder.DEFAULT_JUSTIFICATION, 
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 16), COR_TEXTO));
        utensiliosPanel.setBackground(COR_PANEL);
        utensiliosPanel.setPreferredSize(new Dimension(0, 260));
        cozinhaCentroPanel.add(utensiliosPanel, BorderLayout.SOUTH);
        
        // --- PAINEL DE EVENTOS (ACIMA DA COZINHA) ---
        eventosPanel.setLayout(new BoxLayout(eventosPanel, BoxLayout.Y_AXIS));
        eventosPanel.setBackground(COR_PANEL);
        eventosPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
            "Eventos", TitledBorder.DEFAULT_JUSTIFICATION, 
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 16), COR_TEXTO));
        JScrollPane eventosScroll = new JScrollPane(eventosPanel);
        eventosScroll.setBorder(null);
        eventosScroll.getVerticalScrollBar().setUnitIncrement(16);
        eventosScroll.setPreferredSize(new Dimension(0, 200));
        cozinhaCentroPanel.add(eventosScroll, BorderLayout.NORTH);
        
        // --- PAINEL DE CHEFS (DIREITA) ---
        JPanel chefsPanel = new JPanel();
        chefsPanel.setLayout(new BoxLayout(chefsPanel, BoxLayout.Y_AXIS));
        chefsPanel.setBackground(COR_PANEL);
        chefsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
            "Chefs", TitledBorder.DEFAULT_JUSTIFICATION, 
            TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 16), COR_TEXTO));
        
        // Adiciona os chefs
        for (Chef chef : chefsPreEstabelecidos) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(new Color(245, 245, 245));
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            
            // Ajuste de tamanho do card
            if (chef.getStatus() == Chef.Status.PREPARANDO || chef.getStatus() == Chef.Status.FINALIZANDO) {
                // Tamanho maior para acomodar barra de progresso, utensílios e receita
                card.setMaximumSize(new Dimension(220, 220));
                card.setPreferredSize(new Dimension(220, 220));
            } else {
                // Tamanho menor para chefs livres ou esperando
                card.setMaximumSize(new Dimension(220, 130));
                card.setPreferredSize(new Dimension(220, 130));
            }

            // Nome do chef com ícone
            JLabel nome = new JLabel(chef.getNome() + " ");
            nome.setFont(new Font("Arial", Font.BOLD, 16));
            
            // Habilidade com ícone
            String iconeHabilidade = "";
            switch(chef.getHabilidade()) {
                case RAPIDEZ_PANELA: iconeHabilidade = ""; break;
                case SOBREMESAS: iconeHabilidade = ""; break;
                case RAPIDEZ_FORNO: iconeHabilidade = ""; break;
                case MULTITAREFA: iconeHabilidade = ""; break;
                case PRECISAO: iconeHabilidade = ""; break;
                default: iconeHabilidade = ""; break;
            }
            JLabel hab = new JLabel(iconeHabilidade + " " + chef.getHabilidade().getDescricao());
            hab.setFont(new Font("Arial", Font.ITALIC, 14));
            hab.setForeground(COR_SECUNDARIA);
            
            // Status com cor indicativa
            String statusIcone = "";
            Color statusCor = Color.BLACK;
            switch(chef.getStatus()) {
                case LIVRE: 
                    statusIcone = "✔️"; 
                    statusCor = COR_SECUNDARIA;
                    break;
                case PREPARANDO: 
                    statusIcone = "👨"; 
                    statusCor = COR_PRIMARIA;
                    break;
                case ESPERANDO: 
                    statusIcone = "⌛"; 
                    statusCor = Color.ORANGE;
                    break;
                case FINALIZANDO: 
                    statusIcone = "↻"; 
                    statusCor = new Color(70, 130, 180);
                    break;
                default: 
                    statusIcone = "?"; 
                    statusCor = Color.GRAY;
                    break;
            }
            JLabel status = new JLabel(statusIcone + " Status: " + chef.getStatus().name());
            status.setFont(new Font("Arial", Font.PLAIN, 14));
            status.setForeground(statusCor);
            
            card.add(nome);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            card.add(hab);
            card.add(Box.createRigidArea(new Dimension(0, 5)));
            card.add(status);
            
            // Adiciona informação de utensílios em uso, se houver
            if (!chef.getUtensiliosEmUso().isEmpty()) {
                JLabel utensLabel = new JLabel("🔧 Utensílios em uso:");
                utensLabel.setFont(new Font("Arial", Font.BOLD, 13));
                utensLabel.setForeground(COR_PRIMARIA);
                card.add(Box.createRigidArea(new Dimension(0, 8)));
                card.add(utensLabel);
                
                for (Utensilio u : chef.getUtensiliosEmUso()) {
                    JLabel utensilioLabel = new JLabel("  • " + getIconeUtensilio(u) + " " + u.toString());
                    utensilioLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                    card.add(utensilioLabel);
                }
            }
            
            // Adiciona informação sobre o pedido atual, se estiver preparando
            if (chef.getReceitaAtual() != null) {
                JPanel receitaInfoPanel = new JPanel();
                receitaInfoPanel.setLayout(new BoxLayout(receitaInfoPanel, BoxLayout.Y_AXIS));
                receitaInfoPanel.setOpaque(false);

                JLabel receitaLabel = new JLabel("\uD83D\uDC69\u200D\uD83C\uDF73 Preparando:", SwingConstants.LEFT);
                receitaLabel.setFont(new Font("Arial", Font.BOLD, 12));
                receitaLabel.setForeground(COR_PRIMARIA);
                
                JLabel nomeReceita = new JLabel(chef.getReceitaAtual().getNome(), SwingConstants.LEFT);
                nomeReceita.setFont(new Font("Arial", Font.ITALIC, 11));
                nomeReceita.setForeground(new Color(70, 130, 180));
                
                receitaInfoPanel.add(receitaLabel);
                receitaInfoPanel.add(nomeReceita);
                card.add(Box.createRigidArea(new Dimension(0, 5)));
                card.add(receitaInfoPanel);
            }
            
            chefsPanel.add(card);
            chefsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        JScrollPane chefsScroll = new JScrollPane(chefsPanel);
        chefsScroll.setBorder(null);
        chefsScroll.getVerticalScrollBar().setUnitIncrement(16);
        chefsScroll.setPreferredSize(new Dimension(250, 0));
        
        // Organiza os painéis no painel central
        centroPanel.add(pedidosContainer, BorderLayout.WEST);
        centroPanel.add(cozinhaCentroPanel, BorderLayout.CENTER);
        centroPanel.add(chefsScroll, BorderLayout.EAST);
        
        mainPanel.add(centroPanel, BorderLayout.CENTER);
        
        // --- LOG DE EVENTOS ---
        //logArea.setEditable(false);
        //logArea.setLineWrap(true);
        //logArea.setWrapStyleWord(true);
        //logArea.setFont(new Font("Arial", Font.PLAIN, 14));
        //JScrollPane logScroll = new JScrollPane(logArea);
        //logScroll.setPreferredSize(new Dimension(0, 120));
        //logScroll.setBorder(BorderFactory.createTitledBorder(
        //    BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
        //    "Log de Eventos", TitledBorder.DEFAULT_JUSTIFICATION, 
        //    TitledBorder.DEFAULT_POSITION, new Font("Arial", Font.BOLD, 16), COR_TEXTO));
        //mainPanel.add(logScroll, BorderLayout.SOUTH);
        
        // --- EXIBE A JANELA PRINCIPAL ---
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null); // Centraliza na tela
        frame.setMinimumSize(new Dimension(1280, 800)); // Tamanho mínimo para notebook
        frame.setVisible(true);
        System.out.println(" Fim configurarInterface");
    }

    private String encontrarChefComUtensilio(Utensilio u) {
        List<Chef> chefs = cozinha.getChefs();
        for (Chef chef : chefs) {
            if (chef.getUtensiliosEmUso().contains(u)) {
                return chef.getNome();
            }
        }
        return "";
    }

    private JButton criarBotaoEstilizado(String texto, Color cor) {
        JButton button = new JButton(texto);
        button.setBackground(cor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(cor.brighter());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(cor);
            }
        });
        
        return button;
    }

    private Border criarBordaArredondada(Color cor) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(cor, 2, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    private void liberarUtensilioComAnimacao(Utensilio u) {
        String chefUsando = encontrarChefComUtensilio(u);
        if (!chefUsando.isEmpty()) {
            // Encontra o chef e libera o utensílio
            for (Chef chef : cozinha.getChefs()) {
                if (chef.getNome().equals(chefUsando)) {
                    chef.liberarUtensilio(u);
                    break;
                }
            }
        }

        JButton button = new JButton(getIconeUtensilio(u));
        button.setEnabled(false);
        
        Timer animTimer = new Timer(100, null);
        final int[] count = {0};
        
        animTimer.addActionListener(e -> {
            count[0]++;
            button.setBackground(count[0] % 2 == 0 ? COR_PRIMARIA : COR_DESTAQUE);
            if (count[0] >= 6) {
                animTimer.stop();
                button.setEnabled(true);
                button.setBackground(COR_PRIMARIA);
                logArea.append("\n🔓 Utensílio " + u + " foi liberado manualmente");
                reproduzirSomAcao();
                atualizarPainelCozinha();
            }
        });
        
        animTimer.start();
    }

    private void limparCozinha() {
        for (Utensilio u : Utensilio.values()) {
            String chefUsando = encontrarChefComUtensilio(u);
            if (!chefUsando.isEmpty()) {
                for (Chef chef : cozinha.getChefs()) {
                    if (chef.getNome().equals(chefUsando)) {
                        chef.liberarUtensilio(u);
                    }
                }
            }
        }
        logArea.append("\n🧹 Cozinha foi totalmente limpa!");
        adicionarPontos(5);
        reproduzirSomAcao();
        atualizarPainelCozinha();
    }

    private void ativarBonus() {
        Timer bonusTimer = new Timer(10000, e -> desativarBonus());
        pontuacao *= 2;
        atualizarPontuacao();
        logArea.append("\n⭐ BÔNUS ATIVADO! Pontuação dobrada por 10 segundos!");
        reproduzirSomAcao();
        bonusTimer.setRepeats(false);
        bonusTimer.start();
    }

    private void desativarBonus() {
        pontuacao /= 2;
        atualizarPontuacao();
        logArea.append("\n⭐ Bônus desativado!");
        reproduzirSomAcao();
    }

    private void adicionarPontos(int pontos) {
        pontuacao += pontos;
        pontuacaoLabel.setText(String.format("%d", pontuacao));
        verificarNivel();
        verificarConquistas();
    }

    private void verificarNivel() {
        int novoNivel = (pontuacao / 500) + 1;
        if (novoNivel > nivel) {
            nivel = novoNivel;
            nivelLabel.setText("Nível " + nivel);
            
            // Atualiza a barra de progresso
            int progresso = (pontuacao % 500) * 100 / 500;
            progressBar.setValue(progresso);
            progressBar.setString(progresso + "%");
            
            // Notifica o jogador
            reproduzirSomConquista();
            JOptionPane.showMessageDialog(frame,
                String.format("🎉 NÍVEL %d ALCANÇADO! 🎉\n\n" +
                            "Novas receitas desbloqueadas!\n" +
                            "Bônus de pontuação: +%d%%\n" +
                            "Eventos mais frequentes!\n",
                            nivel, (nivel - 1) * 10),
                "Novo Nível",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Adiciona novas receitas conforme o nível
            atualizarReceitasDisponiveis();
        }
        
        // Atualiza a barra de progresso
        int progresso = (pontuacao % 500) * 100 / 500;
        progressBar.setValue(progresso);
        progressBar.setString(progresso + "%");
    }

    private void atualizarReceitasDisponiveis() {
        receitaCombo.removeAllItems();
        
        // Receitas básicas (sempre disponíveis)
        for (String receita : RECEITAS) {
            receitaCombo.addItem(receita);
        }
        
        // Receitas especiais por nível
        if (nivel >= 2) {
            receitaCombo.addItem("🍖 Churrasco Especial");
            receitaCombo.addItem("🥪 Sanduíche Gourmet");
        }
        if (nivel >= 3) {
            receitaCombo.addItem("🦐 Camarão ao Molho");
            receitaCombo.addItem("🍣 Sushi Variado");
        }
        if (nivel >= 4) {
            receitaCombo.addItem("🥩 Filé à Parmegiana");
            receitaCombo.addItem("🦀 Caranguejo Gratinado");
        }
        if (nivel >= 5) {
            receitaCombo.addItem("🦞 Lagosta ao Thermidor");
            receitaCombo.addItem("🍷 Risoto de Trufas");
        }
    }

    private int calcularPontosPorReceita(String receita) {
        // Pontos base
        int pontos = 50;
        
        // Bônus por nível
        pontos += (nivel - 1) * 5;
        
        // Bônus por complexidade da receita
        if (receita.contains("Gourmet") || receita.contains("Especial")) {
            pontos += 20;
        }
        if (receita.contains("Sushi") || receita.contains("Trufas")) {
            pontos += 30;
        }
        if (receita.contains("Lagosta") || receita.contains("Caranguejo")) {
            pontos += 40;
        }
        
        return pontos;
    }

    private void atualizarTempo() {
        segundosRestantes--;
        int minutos = segundosRestantes / 60;
        int segundos = segundosRestantes % 60;
        timerLabel.setText(String.format("⏱️ Tempo: %d:%02d", minutos, segundos));
        
        if (segundosRestantes <= 0) {
            timer.stop();
            mostrarFimDeJogo();
        }
    }

    private void mostrarFimDeJogo() {
        timer.stop();
        eventoTimer.stop();
        dicasTimer.stop();
        
        // Calcula estatísticas finais
        int conquistasDesbloqueadas = 0;
        for (Conquista c : conquistas) {
            if (c.desbloqueada) conquistasDesbloqueadas++;
        }
        
        double mediapontosPorReceita = totalReceitasPreparadas > 0 ? 
            (double) pontuacao / totalReceitasPreparadas : 0;
        
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("🎮 FIM DE JOGO! 🎮\n\n");
        mensagem.append(String.format(" ESTATÍSTICAS FINAIS:\n\n"));
        mensagem.append(String.format(" Pontuação Final: %d pontos\n", pontuacao));
        mensagem.append(String.format(" Nível Alcançado: %d\n", nivel));
        mensagem.append(String.format(" Total de Receitas: %d\n", totalReceitasPreparadas));
        mensagem.append(String.format(" Média de Pontos/Receita: %.1f\n", mediapontosPorReceita));
        mensagem.append(String.format(" Conquistas Desbloqueadas: %d/%d\n", 
            conquistasDesbloqueadas, conquistas.size()));
        mensagem.append(String.format(" Eventos Participados: %d\n", totalEventosParticipados));
        
        if (!receitaMaisPontos.isEmpty()) {
            mensagem.append(String.format("\n🌟 Melhor Receita:\n%s (%d pontos)\n", 
                receitaMaisPontos, maiorPontuacaoReceita));
        }
        
        // Verifica se é uma das melhores pontuações
        boolean ehRecorde = false;
        if (melhoresPontuacoes.isEmpty() || pontuacao > melhoresPontuacoes.get(0).pontos) {
            ehRecorde = true;
            mensagem.append("\n🏆 NOVO RECORDE! 🏆\n");
        } else if (melhoresPontuacoes.size() < 10 || 
                  pontuacao > melhoresPontuacoes.get(melhoresPontuacoes.size() - 1).pontos) {
            ehRecorde = true;
            mensagem.append("\n✨ Você entrou para o Hall da Fama! ✨\n");
        }
        
        // Salva a pontuação
        salvarPontuacao();

        // Valor final para usar no lambda
        final boolean exibirRanking = ehRecorde;

        // Cria um painel personalizado para a mensagem de fim de jogo
        JPanel fimJogoPanel = new JPanel(new BorderLayout(15, 15));
        fimJogoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Título grande
        JLabel tituloLabel = new JLabel("🎮 FIM DE JOGO!", JLabel.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 24));
        tituloLabel.setForeground(COR_DESTAQUE);
        fimJogoPanel.add(tituloLabel, BorderLayout.NORTH);
        
        // Estatísticas
        JTextArea estatisticasArea = new JTextArea(mensagem.toString());
        estatisticasArea.setEditable(false);
        estatisticasArea.setFont(new Font("Arial", Font.PLAIN, 16));
        estatisticasArea.setBackground(fimJogoPanel.getBackground());
        estatisticasArea.setLineWrap(true);
        estatisticasArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(estatisticasArea);
        scrollPane.setBorder(null);
        fimJogoPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Botões de ação
        JPanel botoesPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        
        JButton btnJogarNovamente = new JButton("Jogar Novamente");
        btnJogarNovamente.setFont(new Font("Arial", Font.BOLD, 16));
        btnJogarNovamente.setBackground(COR_SECUNDARIA);
        btnJogarNovamente.setForeground(Color.BLACK);
        btnJogarNovamente.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JButton btnSair = new JButton("Sair do Jogo");
        btnSair.setFont(new Font("Arial", Font.BOLD, 16));
        btnSair.setBackground(COR_DESTAQUE);
        btnSair.setForeground(Color.BLACK);
        btnSair.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        botoesPanel.add(btnJogarNovamente);
        botoesPanel.add(btnSair);
        fimJogoPanel.add(botoesPanel, BorderLayout.SOUTH);
        
        // Criar e mostrar diálogo
        JDialog dialog = new JDialog(frame, "Fim de Jogo", true);
        dialog.setContentPane(fimJogoPanel);
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(frame);
        
        // Ações dos botões
        btnJogarNovamente.addActionListener(e -> {
            dialog.dispose();
            reiniciarJogo();
        });
        
        btnSair.addActionListener(e -> {
            dialog.dispose();
            if (exibirRanking) {
                mostrarMelhoresPontuacoes();
            }
            System.exit(0);
        });
        
        dialog.setVisible(true);
    }

    private void reiniciarJogo() {
        System.out.println(" Reiniciando jogo...");
        // Limpar todos os dados do jogo
        pontuacao = 0;
        nivel = 1;
        segundosRestantes = 300;
        totalReceitasPreparadas = 0;
        totalEventosParticipados = 0;
        maiorPontuacaoReceita = 0;
        receitaMaisPontos = "";
        
        // Limpar listas e coleções
        pedidosAtivos.clear();
        eventosAtivos.clear();
        
        // Reiniciar conquistas (manter as já desbloqueadas para referência)
        for (Conquista c : conquistas) {
            c.desbloqueada = false;
        }
        
        // Limpar/Reiniciar componentes da interface
        cozinhaPanel.removeAll();
        utensiliosPanel.removeAll();
        eventosPanel.removeAll();
        
        // Reiniciar cozinha e chefs
        for (Chef chef : chefsPreEstabelecidos) {
            chef.setStatus(Chef.Status.LIVRE);
            chef.setReceitaAtual(null);
            new ArrayList<>(chef.getUtensiliosEmUso()).forEach(chef::liberarUtensilio);
        }
        
        // Atualizar elementos visuais
        pontuacaoLabel.setText("0");
        nivelLabel.setText("Nível 1");
        progressBar.setValue(0);
        progressBar.setString("0%");
        
        // Limpar e reiniciar log
        logArea.setText("");
        logArea.append("🎮 Bem-vindo ao Master Chef Ultimate!\n");
        logArea.append("Adicione chefs e prepare deliciosas receitas!\n");
        
        // Reiniciar timers
        if (timer != null) timer.start();
        if (eventoTimer != null) eventoTimer.start();
        if (dicasTimer != null) dicasTimer.start();
        if (pedidoAutoTimer != null) pedidoAutoTimer.start();
        
        // Atualizar receitas disponíveis
        atualizarReceitasDisponiveis();
        
        // Gerar pelo menos um pedido inicial
        gerarPedidoAleatorio();
        
        // Atualizar todos os painéis
        atualizarPainelPedidos();
        atualizarPainelCozinha();
        atualizarPainelEventos();
        
        // Verificar e corrigir layout
        verificarECorrigirLayout();
        
        // Força atualização da interface
        mainPanel.revalidate();
        mainPanel.repaint();
        frame.repaint();
        
        System.out.println(" Jogo reiniciado com sucesso!");
    }

    private void iniciarJogo() {
        carregarPontuacoes();
        timer.start();
        logArea.setText("🎮 Bem-vindo ao Master Chef Ultimate!\n");
        logArea.append("Adicione chefs e prepare deliciosas receitas!\n");
        // Garante pelo menos um pedido ativo ao iniciar
        if (pedidosAtivos.isEmpty()) {
            gerarPedidoAleatorio();
        }
        // Timer para geração automática de pedidos
        if (pedidoAutoTimer == null) {
            pedidoAutoTimer = new Timer(20000, e -> {
                gerarPedidoAleatorio();
                logArea.append("\n🆕 Novo pedido gerado automaticamente!\n");
            });
            pedidoAutoTimer.start();
            this.atualizarPainelPedidos();
        }
    }

    private void inicializarEventos() {
        eventosDisponiveis.add(new EventoEspecial(
            "Hora do Rush",
            "Prepare 3 pratos em 60 segundos para ganhar bônus!",
            "🏃",
            60,
            100
        ));
        
        eventosDisponiveis.add(new EventoEspecial(
            "Cliente VIP",
            "Pratos gourmet valem pontos extras!",
            "👑",
            45,
            50
        ));
        
        eventosDisponiveis.add(new EventoEspecial(
            "Festival Gastronômico",
            "Todos os pratos valem o dobro de pontos!",
            "🎪",
            30,
            0
        ));
        
        eventosDisponiveis.add(new EventoEspecial(
            "Competição MasterChef",
            "Prepare pratos complexos para impressionar os juízes!",
            "🏆",
            50,
            150
        ));

        // Timer para atualizar eventos
        eventoTimer = new Timer(1000, e -> atualizarEventos());
        eventoTimer.start();
    }

    private void gerarEventoAleatorio() {
        if (eventosDisponiveis.isEmpty() || eventosAtivos.size() >= 3) return;

        // Chance de gerar evento aumenta com o nível
        if (random.nextInt(100) > 20 + (nivel * 5)) return;

        EventoEspecial evento = eventosDisponiveis.get(random.nextInt(eventosDisponiveis.size()));
        if (!eventosAtivos.contains(evento)) {
            evento.ativar();
            eventosAtivos.add(evento);
            
            // Notifica o jogador
            reproduzirSomConquista();
            logArea.append(String.format("\n🎉 NOVO EVENTO ESPECIAL!\n%s\n", evento));
            
            // Atualiza o painel de eventos
            atualizarPainelEventos();
        }
    }

    private void atualizarEventos() {
        // Atualiza eventos ativos
        Iterator<EventoEspecial> iterator = eventosAtivos.iterator();
        while (iterator.hasNext()) {
            EventoEspecial evento = iterator.next();
            if (!evento.atualizar()) {
                iterator.remove();
                logArea.append(String.format("\n Evento '%s' encerrado!", evento.nome));
            }
        }
        
        // Chance de gerar novo evento
        if (eventosAtivos.size() < 3 && random.nextInt(100) < 5) {
            EventoEspecial novoEvento = eventosDisponiveis.get(random.nextInt(eventosDisponiveis.size()));
            if (!eventosAtivos.contains(novoEvento)) {
                novoEvento.ativar();
                eventosAtivos.add(novoEvento);
                logArea.append(String.format("\n Novo evento: %s", novoEvento));
                reproduzirSomAcao();
            }
        }
        
        atualizarPainelEventos();
    }

    private void atualizarPainelEventos() {
        eventosPanel.removeAll();
        
        if (eventosAtivos.isEmpty()) {
            JLabel semEventos = new JLabel("Nenhum evento ativo no momento");
            semEventos.setFont(new Font("Arial", Font.ITALIC, 14)); // Aumentado
            semEventos.setForeground(Color.GRAY);
            semEventos.setAlignmentX(Component.CENTER_ALIGNMENT);
            eventosPanel.add(semEventos);
        } else {
            for (EventoEspecial evento : eventosAtivos) {
                JPanel eventoPanel = new JPanel();
                eventoPanel.setLayout(new BoxLayout(eventoPanel, BoxLayout.Y_AXIS));
                eventoPanel.setBackground(new Color(255, 255, 255, 240));
                eventoPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8) // Aumentando o padding
                ));
                
                JLabel nomeLabel = new JLabel(evento.icone + " " + evento.nome);
                nomeLabel.setFont(new Font("Arial", Font.BOLD, 16)); // Aumentado
                nomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel descLabel = new JLabel(evento.descricao);
                descLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Aumentado
                descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                JLabel tempoLabel = new JLabel(String.format("Tempo: %ds", evento.tempoRestante));
                tempoLabel.setFont(new Font("Arial", Font.ITALIC, 14)); // Aumentado
                tempoLabel.setForeground(Color.GRAY);
                tempoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                eventoPanel.add(nomeLabel);
                eventoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                eventoPanel.add(descLabel);
                eventoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                eventoPanel.add(tempoLabel);
                
                eventosPanel.add(eventoPanel);
                eventosPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        
            eventosPanel.revalidate();
            eventosPanel.repaint();
    }

    private int calcularBonusEventos(String receita) {
        int bonus = 0;
        
        for (EventoEspecial evento : eventosAtivos) {
            if (evento.nome.equals("Festival Gastronômico")) {
                bonus += 100; // Dobra os pontos
            }
            else if (evento.nome.equals("Cliente VIP") && 
                    (receita.contains("Gourmet") || receita.contains("Especial"))) {
                bonus += evento.bonusPontos;
            }
            else if (evento.nome.equals("Competição MasterChef") && 
                    (receita.contains("Lagosta") || receita.contains("Trufas"))) {
                bonus += evento.bonusPontos;
            }
        }
        
        return bonus;
    }

    private String getIconeUtensilio(Utensilio u) {
        if (u == null) return "❓";
        
        switch (u) {
            case FACA: return "";
            case PANELA: return "";
            case LIQUIDIFICADOR: return "";
            case COLHER: return "";
            case TIGELA: return "";
            case TABUA: return "";
            case FORNO: return "";
            case BATEDOR: return "";
            case RALADOR: return "";
            case ESPATULA: return "";
            case DESCASCADOR: return "";
            default: return "";
        }
    }

    private void mostrarMensagemErro(String mensagem) {
        JOptionPane.showMessageDialog(frame, mensagem, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private void reproduzirSomAcao() {
        try {
            // Reproduz um beep simples e não bloqueia a interface
            new Thread(() -> {
                try {
        Toolkit.getDefaultToolkit().beep();
                } catch (Exception ex) {
                    // Ignora erro de som
                }
            }).start();
        } catch (Exception e) {
            // Falha silenciosa se não puder reproduzir som
            System.err.println("Não foi possível reproduzir som: " + e.getMessage());
        }
    }

    private void atualizarPontuacao() {
        pontuacaoLabel.setText(String.format("%d", pontuacao));
    }

    private void ativarModoDificil() {
        // Reduz o tempo disponível
        segundosRestantes = Math.max(60, segundosRestantes / 2);
        
        // Dobra os pontos necessários para nível
        nivel = Math.max(1, nivel / 2);
        
        // Adiciona bônus de pontos
        adicionarPontos(50);
        
        logArea.append("\n🔥 MODO DIFÍCIL ATIVADO! Tempo reduzido e pontuação dobrada!");
        reproduzirSomAcao();
    }

    private void salvarPontuacao() {
        // Usar nome do chef do último pedido concluído, se houver
        String nomeChef = "Chef";
        for (int i = pedidosAtivos.size() - 1; i >= 0; i--) {
            Pedido p = pedidosAtivos.get(i);
            if (p.getStatus() == Pedido.StatusPedido.CONCLUIDO && p.getChefDesignado() != null) {
                nomeChef = p.getChefDesignado().getNome();
                break;
            }
        }
        Pontuacao novaPontuacao = new Pontuacao(nomeChef, pontuacao);
        melhoresPontuacoes.add(novaPontuacao);
        Collections.sort(melhoresPontuacoes);
        
        // Manter apenas as 10 melhores pontuações
        if (melhoresPontuacoes.size() > 10) {
            melhoresPontuacoes = melhoresPontuacoes.subList(0, 10);
        }

        // Salvar em arquivo
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO_PONTUACOES))) {
            for (Pontuacao p : melhoresPontuacoes) {
                writer.println(p.nomeJogador + "," + p.pontos + "," + p.data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarPontuacoes() {
        melhoresPontuacoes.clear();
        File arquivo = new File(ARQUIVO_PONTUACOES);
        
        if (!arquivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivo))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length == 3) {
                    Pontuacao p = new Pontuacao(partes[0], Integer.parseInt(partes[1]));
                    melhoresPontuacoes.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarMelhoresPontuacoes() {
        StringBuilder sb = new StringBuilder();
        sb.append("🏆 MELHORES PONTUAÇÕES 🏆\n\n");
        
        if (melhoresPontuacoes.isEmpty()) {
            sb.append("Ainda não há pontuações registradas!");
        } else {
            for (int i = 0; i < melhoresPontuacoes.size(); i++) {
                sb.append(String.format("%d. %s\n", i + 1, melhoresPontuacoes.get(i)));
            }
        }

        JOptionPane.showMessageDialog(frame, sb.toString(),
            "Hall da Fama", JOptionPane.INFORMATION_MESSAGE);
    }

    private void inicializarConquistas() {
        conquistas.add(new Conquista("Cozinheiro Iniciante", "Alcance 100 pontos", "👶", 100));
        conquistas.add(new Conquista("Chef Amador", "Alcance 500 pontos", "👨‍🍳", 500));
        conquistas.add(new Conquista("Chef Profissional", "Alcance 1000 pontos", "🎯", 1000));
        conquistas.add(new Conquista("Master Chef", "Alcance 2000 pontos", "👑", 2000));
        conquistas.add(new Conquista("Lenda da Cozinha", "Alcance 5000 pontos", "🌟", 5000));
    }

    private void inicializarDicas() {
        dicas.add("💡 Dica: Mantenha seus chefs organizados para evitar conflitos de utensílios!");
        dicas.add("💡 Dica: Preste atenção aos eventos especiais para ganhar mais pontos!");
        dicas.add("💡 Dica: Receitas mais complexas valem mais pontos!");
        dicas.add("💡 Dica: Libere utensílios assim que possível para outros chefs usarem!");
        dicas.add("💡 Dica: O modo difícil oferece mais pontos, mas é mais desafiador!");
        dicas.add("💡 Dica: Mantenha-se atento ao tempo restante!");
        dicas.add("💡 Dica: Eventos especiais podem aparecer a qualquer momento!");
    }

    private void mostrarDicaAleatoria() {
        if (!dicas.isEmpty()) {
            String dica = dicas.get(random.nextInt(dicas.size()));
            logArea.append("\n" + dica + "\n");
        }
    }

    private void verificarConquistas() {
        for (Conquista c : conquistas) {
            if (c.verificar(pontuacao)) {
                reproduzirSomConquista();
                JOptionPane.showMessageDialog(frame,
                    String.format(" CONQUISTA DESBLOQUEADA! \n\n%s", c),
                    "Nova Conquista",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void mostrarConquistas() {
        StringBuilder sb = new StringBuilder();
        sb.append(" CONQUISTAS \n\n");
        
        for (Conquista c : conquistas) {
            sb.append(String.format("%s %s\n", 
                c.desbloqueada ? "✅" : "❌",
                c));
        }

        JOptionPane.showMessageDialog(frame, sb.toString(),
            "Conquistas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void reproduzirSomConquista() {
        try {
            // Reproduz sequência de beeps em thread separada
            new Thread(() -> {
                try {
                    for (int i = 0; i < 3; i++) {
                        Toolkit.getDefaultToolkit().beep();
                        Thread.sleep(100);
                    }
                } catch (Exception ex) {
                    // Ignora erro de som ou interrupção
                }
            }).start();
        } catch (Exception e) {
            // Falha silenciosa
            System.err.println("Não foi possível reproduzir som: " + e.getMessage());
        }
    }

    private void mostrarReceitasEUtensilios() {
        StringBuilder sb = new StringBuilder();
        sb.append(" RECEITAS E UTENSÍLIOS \n\n");
        for (String receita : RECEITAS) {
        List<Utensilio> utensilios = new ArrayList<>();
            String receitaLower = receita.toLowerCase();
            if (receitaLower.contains("omelete")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.PANELA);
                utensilios.add(Utensilio.COLHER);
            } else if (receitaLower.contains("vitamina") || receitaLower.contains("suco")) {
                utensilios.add(Utensilio.LIQUIDIFICADOR);
                utensilios.add(Utensilio.TIGELA);
            } else if (receitaLower.contains("sopa")) {
                utensilios.add(Utensilio.PANELA);
                utensilios.add(Utensilio.COLHER);
                utensilios.add(Utensilio.TABUA);
            } else if (receitaLower.contains("bolo")) {
                utensilios.add(Utensilio.BATEDOR);
                utensilios.add(Utensilio.TIGELA);
                utensilios.add(Utensilio.FORNO);
            } else if (receitaLower.contains("lasanha")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.FORNO);
                utensilios.add(Utensilio.ESPATULA);
            } else if (receitaLower.contains("salada")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.DESCASCADOR);
                utensilios.add(Utensilio.TABUA);
            } else if (receitaLower.contains("risoto")) {
                utensilios.add(Utensilio.PANELA);
                utensilios.add(Utensilio.COLHER);
                utensilios.add(Utensilio.RALADOR);
            } else if (receitaLower.contains("taco")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.TABUA);
                utensilios.add(Utensilio.PANELA);
            } else if (receitaLower.contains("pizza")) {
                utensilios.add(Utensilio.FORNO);
                utensilios.add(Utensilio.ESPATULA);
                utensilios.add(Utensilio.TABUA);
            } else if (receitaLower.contains("lámen") || receitaLower.contains("lamen")) {
                utensilios.add(Utensilio.PANELA);
                utensilios.add(Utensilio.COLHER);
                utensilios.add(Utensilio.TIGELA);
            } else if (receitaLower.contains("churrasco")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.ESPATULA);
                utensilios.add(Utensilio.TABUA);
            } else if (receitaLower.contains("sanduíche")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.TABUA);
                utensilios.add(Utensilio.TIGELA);
            } else if (receitaLower.contains("camarão") || receitaLower.contains("camarao")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.PANELA);
                utensilios.add(Utensilio.COLHER);
            } else if (receitaLower.contains("sushi")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.TABUA);
                utensilios.add(Utensilio.ESPATULA);
            } else if (receitaLower.contains("filé") || receitaLower.contains("file")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.PANELA);
                utensilios.add(Utensilio.TABUA);
            } else if (receitaLower.contains("caranguejo")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.PANELA);
                utensilios.add(Utensilio.COLHER);
            } else if (receitaLower.contains("lagosta")) {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.PANELA);
                utensilios.add(Utensilio.FORNO);
            } else if (receitaLower.contains("trufa")) {
                utensilios.add(Utensilio.RALADOR);
                utensilios.add(Utensilio.TIGELA);
                utensilios.add(Utensilio.PANELA);
            } else {
                utensilios.add(Utensilio.FACA);
                utensilios.add(Utensilio.TABUA);
            }
            sb.append("\n" + receita + ": ");
            for (Utensilio u : utensilios) {
                sb.append(u.getIcone()).append(" ").append(u.name()).append("  ");
            }
            sb.append("\n");
        }
        JOptionPane.showMessageDialog(frame, sb.toString(), "Receitas & Utensílios", JOptionPane.INFORMATION_MESSAGE);
    }

    private void atualizarPainelPedidos() {
        pedidosPanel.removeAll();
        for (Pedido pedido : pedidosAtivos) {
            JPanel p = new JPanel(new BorderLayout(0, 5));
            p.setBackground(new Color(255,255,255,220));
            p.setMaximumSize(new Dimension(460, 150)); // Limite de largura para celular
            p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_PRIMARIA, 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));
            
            // Cabeçalho com cliente e status
            JPanel headerPanel = new JPanel(new BorderLayout(5, 0));
            headerPanel.setOpaque(false);
            
            // Cliente
            JLabel clienteLabel = new JLabel(pedido.getCliente());
            clienteLabel.setFont(new Font("Arial", Font.BOLD, 14));
            headerPanel.add(clienteLabel, BorderLayout.WEST);
            
            // Status
            Color statusColor;
            String statusTexto;
            switch (pedido.getStatus()) {
                case PENDENTE: 
                    statusColor = Color.ORANGE; 
                    statusTexto = " Pendente";
                break;
                case EM_PREPARO: 
                    statusColor = COR_PRIMARIA; 
                    statusTexto = " Preparando";
                    break;
                case CONCLUIDO: 
                    statusColor = COR_SECUNDARIA; 
                    statusTexto = " Concluído";
                    break;
                default: 
                    statusColor = COR_TEXTO;
                    statusTexto = "Status desconhecido";
            }
            JLabel statusLabel = new JLabel(statusTexto);
            statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
            statusLabel.setForeground(statusColor);
            headerPanel.add(statusLabel, BorderLayout.EAST);
            
            p.add(headerPanel, BorderLayout.NORTH);
            
            // Nome da receita
            JLabel receitaLabel = new JLabel(pedido.getReceita().getNome());
            receitaLabel.setFont(new Font("Arial", Font.BOLD, 13));
            receitaLabel.setForeground(COR_PRIMARIA);
            p.add(receitaLabel, BorderLayout.CENTER);
            
            // Painel de ações e informações adicionais
            JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
            bottomPanel.setOpaque(false);
            
            // Utensílios
            StringBuilder utens = new StringBuilder();
            for (Utensilio u : pedido.getReceita().getUtensilios()) 
                utens.append(u.getIcone()).append(" ");
            JLabel utensLabel = new JLabel(utens.toString());
            utensLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            bottomPanel.add(utensLabel, BorderLayout.WEST);
            
            // Painel de ações específicas por status
            if (pedido.getStatus() == Pedido.StatusPedido.PENDENTE) {
                JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                actionPanel.setOpaque(false);
                
                // Filtra apenas chefs livres
                List<Chef> livres = new ArrayList<>();
                for (Chef chef : chefsPreEstabelecidos) {
                    if (chef.getStatus() == Chef.Status.LIVRE) {
                        livres.add(chef);
                    }
                }
                
                if (livres.isEmpty()) {
                    JLabel aviso = new JLabel("Sem chefs disponíveis");
                    aviso.setFont(new Font("Arial", Font.ITALIC, 11));
                    aviso.setForeground(COR_DESTAQUE);
                    actionPanel.add(aviso);
                } else {
                    // ComboBox compacto apenas com nomes
                    JComboBox<Chef> chefsCombo = new JComboBox<>(livres.toArray(new Chef[0]));
                    chefsCombo.setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                            if (value instanceof Chef) {
                                Chef c = (Chef) value;
                                label.setText(c.getNome());
                            }
                            return label;
                        }
                    });
                    chefsCombo.setPreferredSize(new Dimension(80, 25));
                    
                    JButton designarBtn = criarBotaoEstilizado("✓", COR_SECUNDARIA);
                    designarBtn.setFont(new Font("Arial", Font.BOLD, 12));
                    designarBtn.setPreferredSize(new Dimension(30, 25));
                    
                    designarBtn.addActionListener(e -> {
                        Chef chef = (Chef) chefsCombo.getSelectedItem();
                        if (chef == null) {
                            mostrarMensagemErro("Nenhum chef disponível!");
                            return;
                        }
                        // Designa o chef para o pedido
                        pedido.designarChef(chef);
                        chef.setReceitaAtual(pedido.getReceita());
                        // Inicia o progresso de preparação
                        iniciarPreparacaoPedido(chef, pedido);
                        logArea.append(String.format("\n %s designado para %s!\n", chef.getNome(), pedido.getCliente()));
                        atualizarPainelPedidos();
                        atualizarPainelCozinha();
                    });
                    
                    actionPanel.add(chefsCombo);
                    actionPanel.add(designarBtn);
                }
                
                bottomPanel.add(actionPanel, BorderLayout.EAST);
            } else if (pedido.getStatus() == Pedido.StatusPedido.EM_PREPARO) {
                JLabel chefLabel = new JLabel(pedido.getChefDesignado() != null ? pedido.getChefDesignado().getNome() : "-");
                chefLabel.setFont(new Font("Arial", Font.BOLD, 12));
                chefLabel.setForeground(COR_PRIMARIA);
                bottomPanel.add(chefLabel, BorderLayout.EAST);
            } else if (pedido.getStatus() == Pedido.StatusPedido.CONCLUIDO) {
                JLabel avaliacaoLabel = new JLabel(String.format("%.1f ★", pedido.getAvaliacao()));
                avaliacaoLabel.setFont(new Font("Arial", Font.BOLD, 14));
                avaliacaoLabel.setForeground(Color.ORANGE);
                bottomPanel.add(avaliacaoLabel, BorderLayout.EAST);
            }
            
            p.add(bottomPanel, BorderLayout.SOUTH);
            
            pedidosPanel.add(p);
            pedidosPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        
        if (pedidosAtivos.isEmpty()) {
            JLabel semPedidos = new JLabel("Sem pedidos ativos. Clique em 'Novo Pedido'");
            semPedidos.setFont(new Font("Arial", Font.ITALIC, 14));
            semPedidos.setForeground(Color.GRAY);
            semPedidos.setAlignmentX(Component.CENTER_ALIGNMENT);
            pedidosPanel.add(semPedidos);
        }
        
        pedidosPanel.revalidate();
        pedidosPanel.repaint();
    }
    
    private void iniciarPreparacaoPedido(Chef chef, Pedido pedido) {
        try {
            // Define o pedido como EM_PREPARO explicitamente (apenas aqui)
            pedido.setStatus(Pedido.StatusPedido.EM_PREPARO);
            atualizarPainelPedidos();
            
            // Registra no log que o chef começou a preparar
            logArea.append(String.format("\n %s começou a preparar o pedido de %s!\n", 
                chef.getNome(), pedido.getCliente()));
            
            // Configura o callback de finalização
            chef.setAoFinalizar(() -> {
                try {
                    // Finaliza o pedido com uma avaliação baseada na eficiência do chef
                    pedido.concluir(chef.getEficiencia() * 20);
                    chef.setStatus(Chef.Status.LIVRE);
                    
                    // Libera todos os utensílios do chef
                    new ArrayList<>(chef.getUtensiliosEmUso()).forEach(chef::liberarUtensilio);
                    
                    // Registra no log
                    logArea.append(String.format("\n Pedido de %s concluído por %s! Avaliação: %.1f\n", 
                        pedido.getCliente(), chef.getNome(), pedido.getAvaliacao()));
                    
                    // Adiciona pontos
                    int pontos = calcularPontosPorReceita(pedido.getReceita().getNome());
                    adicionarPontos(pontos);
                    
                    // Atualiza estatísticas
                    totalReceitasPreparadas++;
                    if (pontos > maiorPontuacaoReceita) {
                        maiorPontuacaoReceita = pontos;
                        receitaMaisPontos = pedido.getReceita().getNome();
                    }
                    
                    // Atualiza interface
                    atualizarPainelPedidos();
                    atualizarPainelCozinha();
                    
                    
                } catch (Exception e) {
                    logArea.append("\n❌ Erro ao finalizar pedido: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // Adiciona utensílios automaticamente se estiverem disponíveis
            boolean todosUtensiliosObtidos = true;
            for (Utensilio u : pedido.getReceita().getUtensilios()) {
                try {
                    if (cozinha.checarUtensilioDisponivel(u)) {
                        chef.usarUtensilio(u);
                    } else {
                        todosUtensiliosObtidos = false;
                        logArea.append("\n⚠️ " + chef.getNome() + " aguardando por " + u);
                    }
                } catch (Exception e) {
                    todosUtensiliosObtidos = false;
                    logArea.append("\n⚠️ " + chef.getNome() + " não conseguiu obter " + u);
                }
            }
            
            // Se não conseguiu todos os utensílios, seta status adequado
            if (!todosUtensiliosObtidos) {
                chef.setStatus(Chef.Status.ESPERANDO);
            } else {
                chef.setStatus(Chef.Status.PREPARANDO);
            }
            
            // Inicia o progresso de preparação usando um timer
            Timer progressoTimer = new Timer(500, null); // Atualiza a cada meio segundo para ser mais responsivo
            final int[] progresso = {0};
            chef.iniciarProgresso(); // Zera o progresso atual
            
            progressoTimer.addActionListener(e -> {
                // Adiciona um log para o nome do pedido e do chef
                String logPrefix = String.format("[ProgTimer Pedido:%s Chef:%s] ", pedido.getCliente(), chef.getNome());

                if (chef.getStatus() == Chef.Status.PREPARANDO || chef.getStatus() == Chef.Status.FINALIZANDO) {
                    int incremento = (int)(chef.getEficiencia() * 4);
                    progresso[0] += incremento;
                    chef.atualizarProgresso(progresso[0]);
                    logArea.append(String.format("\n%sProgresso: %d%% (incremento: %d)", logPrefix, progresso[0], incremento));
                    
                    atualizarPainelCozinha();
                    atualizarPainelPedidos();
                    
                    if (progresso[0] >= 80 && chef.getStatus() != Chef.Status.FINALIZANDO) {
                        logArea.append(String.format("\n%sMudando para FINALIZANDO (progresso >= 80%%)", logPrefix));
                        chef.setStatus(Chef.Status.FINALIZANDO);
                    }
                    
                    if (progresso[0] >= 100) {
                        logArea.append(String.format("\n%sProgresso >= 100%%. Parando timer e chamando aoFinalizar.", logPrefix));
                        progressoTimer.stop();
                        chef.atualizarProgresso(100);
                        
                        if (chef.getStatus() != Chef.Status.FINALIZANDO) {
                            logArea.append(String.format("\n%sForçando status para FINALIZANDO antes de aoFinalizar.", logPrefix));
                            chef.setStatus(Chef.Status.FINALIZANDO);
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            String callbackLogPrefix = String.format("[aoFinalizar Pedido:%s Chef:%s] ", pedido.getCliente(), chef.getNome());
                            logArea.append(String.format("\n%sIniciando execução do callback aoFinalizar.", callbackLogPrefix));
                            System.out.println(callbackLogPrefix + "Iniciando execução do callback aoFinalizar.");
                            try {
                                chef.aoFinalizar();
                                logArea.append(String.format("\n%sCallback aoFinalizar executado. Pedido status: %s, Chef status: %s", 
                                    callbackLogPrefix, pedido.getStatus(), chef.getStatus()));
                                
                                if (pedido.getStatus() != Pedido.StatusPedido.CONCLUIDO) {
                                    logArea.append(String.format("\n%s⚠️ Forçando conclusão do pedido pós-callback. Status atual: %s", 
                                        callbackLogPrefix, pedido.getStatus()));
                                    pedido.concluir(chef.getEficiencia() * 20);
                                    chef.setStatus(Chef.Status.LIVRE);
                                    logArea.append(String.format("\n%sPedido forçado para CONCLUIDO. Chef para LIVRE.", callbackLogPrefix));
                                }
                                
                                atualizarPainelPedidos();
                                atualizarPainelCozinha();
                            } catch (Exception ex) {
                                logArea.append(String.format("\n%s❌ Erro no callback aoFinalizar: %s", callbackLogPrefix, ex.getMessage()));
                                ex.printStackTrace();
                                try {
                                    logArea.append(String.format("\n%sTentando forçar conclusão do pedido após erro no callback.", callbackLogPrefix));
                                    pedido.concluir(chef.getEficiencia() * 20);
                                    chef.setStatus(Chef.Status.LIVRE);
                                    atualizarPainelPedidos();
                                    atualizarPainelCozinha();
                                } catch (Exception innerEx) {
                                    logArea.append(String.format("\n%s❌ Erro fatal ao tentar recuperar pedido pós-erro callback: %s", 
                                        callbackLogPrefix, innerEx.getMessage()));
                                }
                            }
                        });
                    }
                } else if (chef.getStatus() == Chef.Status.ESPERANDO) {
                    logArea.append(String.format("\n%sChef está ESPERANDO. Verificando utensílios...", logPrefix));
                    List<Utensilio> utensiliosFaltantes = new ArrayList<>(pedido.getReceita().getUtensilios());
                    utensiliosFaltantes.removeAll(chef.getUtensiliosEmUso());

                    if (utensiliosFaltantes.isEmpty()) { 
                        logArea.append(String.format("\n%sTodos utensílios obtidos! Mudando para PREPARANDO.", logPrefix));
                        chef.setStatus(Chef.Status.PREPARANDO);
                    } else {
                        logArea.append(String.format("\n%sAinda faltam utensílios: %s. Tentando obter...", logPrefix, utensiliosFaltantes));
                        boolean obteveAlgumUtensilioNovoNestaRodada = false;
                        for (Utensilio uFaltante : utensiliosFaltantes) {
                            if (cozinha.checarUtensilioDisponivel(uFaltante)) {
                                try {
                                    if (cozinha.tentarPegarUtensilio(chef, uFaltante)) {
                                        chef.adicionarUtensilioObtido(uFaltante); 
                                        logArea.append(String.format("\n%sChef obteve %s.", logPrefix, uFaltante));
                                        obteveAlgumUtensilioNovoNestaRodada = true;
                                    } else {
                                        logArea.append(String.format("\n%sFalha ao tentar pegar %s (cozinha.tentarPegarUtensilio retornou false).", logPrefix, uFaltante));
                                    }
                                } catch (Exception ex) { 
                                     logArea.append(String.format("\n%sExceção ao tentar pegar %s: %s", logPrefix, uFaltante, ex.getMessage()));
                                }
                            } else {
                                 logArea.append(String.format("\n%sUtensílio %s não disponível na cozinha.", logPrefix, uFaltante));
                            }
                        }

                        List<Utensilio> utensiliosAindaFaltantes = new ArrayList<>(pedido.getReceita().getUtensilios());
                        utensiliosAindaFaltantes.removeAll(chef.getUtensiliosEmUso());

                        if (utensiliosAindaFaltantes.isEmpty()) {
                            logArea.append(String.format("\n%sTODOS utensílios obtidos após nova tentativa! Mudando para PREPARANDO.", logPrefix));
                            chef.setStatus(Chef.Status.PREPARANDO);
                        } else if (obteveAlgumUtensilioNovoNestaRodada) {
                            logArea.append(String.format("\n%sPegou alguns, mas ainda ESPERANDO por: %s.", logPrefix, utensiliosAindaFaltantes));
                            atualizarPainelCozinha();
                            atualizarPainelPedidos(); 
                        } else {
                            logArea.append(String.format("\n%sNenhum novo utensílio pego. Continua ESPERANDO por: %s.", logPrefix, utensiliosAindaFaltantes));
                        }
                    }
                } else {
                     logArea.append(String.format("\n%sChef em estado inesperado %s. Timer continua.", logPrefix, chef.getStatus()));
                }
            });
            
            progressoTimer.setRepeats(true);
            progressoTimer.setDelay(500);
            progressoTimer.start();
            
        } catch (Exception ex) {
            // Em caso de erro, registra no log
            logArea.append("\n❌ Erro ao iniciar preparação: " + ex.getMessage());
            System.err.println("Erro ao iniciar preparação: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void gerarPedidoAleatorio() {
        // Limite máximo de pedidos ativos - remover isso permite mais pedidos
        // if (pedidosAtivos.size() >= LIMITE_PRATOS_SIMULTANEOS) return;
        
        String[] nomesClientes = {"Patrick", "Neymar", "Beatriz", "Felipe", "Marina", "Pedro", "Florentina", "Rafael", "Sofia", "Tiago"};
        String cliente = nomesClientes[new Random().nextInt(nomesClientes.length)];
        String receitaNome = RECEITAS[new Random().nextInt(RECEITAS.length)];
        List<Utensilio> utensiliosReceita = new ArrayList<>();
        String receitaLower = receitaNome.toLowerCase();
        if (receitaLower.contains("omelete")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.PANELA); utensiliosReceita.add(Utensilio.COLHER); }
        else if (receitaLower.contains("vitamina") || receitaLower.contains("suco")) { utensiliosReceita.add(Utensilio.LIQUIDIFICADOR); utensiliosReceita.add(Utensilio.TIGELA); }
        else if (receitaLower.contains("sopa")) { utensiliosReceita.add(Utensilio.PANELA); utensiliosReceita.add(Utensilio.COLHER); utensiliosReceita.add(Utensilio.TABUA); }
        else if (receitaLower.contains("bolo")) { utensiliosReceita.add(Utensilio.BATEDOR); utensiliosReceita.add(Utensilio.TIGELA); utensiliosReceita.add(Utensilio.FORNO); }
        else if (receitaLower.contains("lasanha")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.FORNO); utensiliosReceita.add(Utensilio.ESPATULA); }
        else if (receitaLower.contains("salada")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.DESCASCADOR); utensiliosReceita.add(Utensilio.TABUA); }
        else if (receitaLower.contains("risoto")) { utensiliosReceita.add(Utensilio.PANELA); utensiliosReceita.add(Utensilio.COLHER); utensiliosReceita.add(Utensilio.RALADOR); }
        else if (receitaLower.contains("taco")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.TABUA); utensiliosReceita.add(Utensilio.PANELA); }
        else if (receitaLower.contains("pizza")) { utensiliosReceita.add(Utensilio.FORNO); utensiliosReceita.add(Utensilio.ESPATULA); utensiliosReceita.add(Utensilio.TABUA); }
        else if (receitaLower.contains("lámen") || receitaLower.contains("lamen")) { utensiliosReceita.add(Utensilio.PANELA); utensiliosReceita.add(Utensilio.COLHER); utensiliosReceita.add(Utensilio.TIGELA); }
        else if (receitaLower.contains("churrasco")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.ESPATULA); utensiliosReceita.add(Utensilio.TABUA); }
        else if (receitaLower.contains("sanduíche")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.TABUA); utensiliosReceita.add(Utensilio.TIGELA); }
        else if (receitaLower.contains("camarão") || receitaLower.contains("camarao")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.PANELA); utensiliosReceita.add(Utensilio.COLHER); }
        else if (receitaLower.contains("sushi")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.TABUA); utensiliosReceita.add(Utensilio.ESPATULA); }
        else if (receitaLower.contains("filé") || receitaLower.contains("file")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.PANELA); utensiliosReceita.add(Utensilio.TABUA); }
        else if (receitaLower.contains("caranguejo")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.PANELA); utensiliosReceita.add(Utensilio.COLHER); }
        else if (receitaLower.contains("lagosta")) { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.PANELA); utensiliosReceita.add(Utensilio.FORNO); }
        else if (receitaLower.contains("trufa")) { utensiliosReceita.add(Utensilio.RALADOR); utensiliosReceita.add(Utensilio.TIGELA); utensiliosReceita.add(Utensilio.PANELA); }
        else { utensiliosReceita.add(Utensilio.FACA); utensiliosReceita.add(Utensilio.TABUA); }
        Receita receita = new Receita(receitaNome, utensiliosReceita);
        Pedido pedido = new Pedido(cliente, receita, 120, new Random().nextBoolean());
        pedidosAtivos.add(pedido);
        atualizarPainelPedidos();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new JogoCozinha());
    }

    private void inicializarChefsPreEstabelecidos() {
        chefsPreEstabelecidos.clear();
        chefsPreEstabelecidos.add(new Chef("Ana", Chef.Habilidade.RAPIDEZ_PANELA, cozinha));
        chefsPreEstabelecidos.add(new Chef("João", Chef.Habilidade.SOBREMESAS, cozinha));
        chefsPreEstabelecidos.add(new Chef("Joana", Chef.Habilidade.RAPIDEZ_FORNO, cozinha));
        chefsPreEstabelecidos.add(new Chef("Carlos", Chef.Habilidade.MULTITAREFA, cozinha));
        chefsPreEstabelecidos.add(new Chef("Lucia", Chef.Habilidade.PRECISAO, cozinha));
    }

    private List<Chef> chefsPreEstabelecidos = new ArrayList<>();

    private void atualizarPainelCozinha() {
        try {
            // ----- Atualizar Sprites dos Chefs e objetos visuais no LayeredPane -----
            
            // ----- Atualizar painel de utensílios -----
            utensiliosPanel.removeAll();
            utensiliosPanel.setLayout(new GridLayout(2, 5, 10, 10));
            for (Utensilio u : Utensilio.values()) {
                JPanel utensilioPanel = new JPanel(new BorderLayout(5, 2));
                utensilioPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
                utensilioPanel.setBackground(COR_PANEL);
                utensilioPanel.setPreferredSize(new Dimension(140, 70));
                Chef chefUsando = cozinha.getChefComUtensilio(u);
                Color statusColor = chefUsando == null ? COR_SECUNDARIA : COR_DESTAQUE;
                JLabel label = new JLabel(getIconeUtensilio(u) + " " + u.toString(), SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 12));
                label.setForeground(statusColor);
                utensilioPanel.add(label, BorderLayout.CENTER);
                if (chefUsando != null) {
                    JPanel bottomPanel = new JPanel(new GridBagLayout());
                    bottomPanel.setOpaque(false);
                    bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.anchor = GridBagConstraints.CENTER;
                    gbc.insets = new Insets(0, 0, 2, 0); // Espaço pequeno entre os elementos

                    JLabel usoLabel = new JLabel("Em uso por: " + chefUsando.getNome());
                    usoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
                    usoLabel.setForeground(COR_DESTAQUE);
                    bottomPanel.add(usoLabel, gbc);

                    gbc.gridy = 1;
                    gbc.insets = new Insets(2, 0, 0, 0); // Espaço pequeno acima do botão
                    JButton liberarBtn = criarBotaoEstilizado("Liberar", COR_DESTAQUE);
                    liberarBtn.setFont(new Font("Arial", Font.PLAIN, 12));
                    liberarBtn.setPreferredSize(new Dimension(90, 28));
                    liberarBtn.addActionListener(e -> {
                        chefUsando.liberarUtensilio(u);
                        logArea.append(String.format("\n Utensílio %s liberado!\n", u));
                        atualizarPainelCozinha();
                    });
                    bottomPanel.add(liberarBtn, gbc);

                    utensilioPanel.add(bottomPanel, BorderLayout.SOUTH);
                } else {
                    JLabel livreLabel = new JLabel("Livre", SwingConstants.CENTER);
                    livreLabel.setFont(new Font("Arial", Font.ITALIC, 10));
                    livreLabel.setForeground(COR_SECUNDARIA);
                    utensilioPanel.add(livreLabel, BorderLayout.SOUTH);
                }
                utensiliosPanel.add(utensilioPanel);
            }
            
            // ----- Atualizar painel de chefs (caso esteja usando layout de notebook) -----
            for (Component comp : mainPanel.getComponents()) {
                if (comp instanceof JPanel && comp != mainPanel.getComponent(0)) { // Não é o header
                    JPanel centroPanel = (JPanel) comp;
                    for (Component subComp : centroPanel.getComponents()) {
                        if (subComp instanceof JScrollPane) {
                            JScrollPane scrollPane = (JScrollPane) subComp;
                            Component viewComp = scrollPane.getViewport().getView();
                            if (viewComp instanceof JPanel) {
                                JPanel panel = (JPanel) viewComp;
                                if (panel.getBorder() instanceof TitledBorder) {
                                    TitledBorder border = (TitledBorder) panel.getBorder();
                                    if (border.getTitle().contains("Chefs")) {
                                        // Atualiza o painel de chefs
                                        panel.removeAll();
                                        
                                        for (Chef chef : chefsPreEstabelecidos) {
                                            JPanel card = new JPanel();
                                            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                                            card.setBackground(new Color(245, 245, 245));
                                            card.setBorder(BorderFactory.createCompoundBorder(
                                                BorderFactory.createLineBorder(COR_PRIMARIA, 2, true),
                                                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                                            
                                            // Ajuste de tamanho do card quando o chef está PREPARANDO ou FINALIZANDO
                                            if (chef.getStatus() == Chef.Status.PREPARANDO || chef.getStatus() == Chef.Status.FINALIZANDO) {
                                                card.setMaximumSize(new Dimension(220, 180));
                                                card.setPreferredSize(new Dimension(220, 180));
                                            } else {
                                                card.setMaximumSize(new Dimension(220, 130));
                                                card.setPreferredSize(new Dimension(220, 130));
                                            }

                                            // Nome do chef com ícone
                                            JLabel nome = new JLabel(chef.getNome() + " ");
                                            nome.setFont(new Font("Arial", Font.BOLD, 16));
                                            
                                            // Habilidade com ícone
                                            String iconeHabilidade = "";
                                            switch(chef.getHabilidade()) {
                                                case RAPIDEZ_PANELA: iconeHabilidade = ""; break;
                                                case SOBREMESAS: iconeHabilidade = ""; break;
                                                case RAPIDEZ_FORNO: iconeHabilidade = ""; break;
                                                case MULTITAREFA: iconeHabilidade = ""; break;
                                                case PRECISAO: iconeHabilidade = ""; break;
                                                default: iconeHabilidade = ""; break;
                                            }
                                            JLabel hab = new JLabel(iconeHabilidade + " " + chef.getHabilidade().getDescricao());
                                            hab.setFont(new Font("Arial", Font.ITALIC, 14));
                                            hab.setForeground(COR_SECUNDARIA);
                                            
                                            // Status com cor indicativa
                                            String statusIcone = "";
                                            Color statusCor = Color.BLACK;
                                            switch(chef.getStatus()) {
                                                case LIVRE: 
                                                    statusIcone = "✅"; 
                                                    statusCor = COR_SECUNDARIA;
                                                    break;
                                                case PREPARANDO: 
                                                    statusIcone = "👨‍🍳"; 
                                                    statusCor = COR_PRIMARIA;
                                                    break;
                                                case ESPERANDO: 
                                                    statusIcone = "⏳"; 
                                                    statusCor = Color.ORANGE;
                                                    break;
                                                case FINALIZANDO: 
                                                    statusIcone = "🔄"; 
                                                    statusCor = new Color(70, 130, 180);
                                                    break;
                                                default: 
                                                    statusIcone = "❓"; 
                                                    statusCor = Color.GRAY;
                                                    break;
                                            }
                                            JLabel status = new JLabel(statusIcone + " Status: " + chef.getStatus().name());
                                            status.setFont(new Font("Arial", Font.PLAIN, 14));
                                            status.setForeground(statusCor);
                                            
                                            // Progresso, se estiver preparando algo
                                            if (chef.getStatus() == Chef.Status.PREPARANDO || chef.getStatus() == Chef.Status.FINALIZANDO) {
                                                JProgressBar progressBar = new JProgressBar(0, 100);
                                                progressBar.setValue(chef.getProgressoAtual());
                                                progressBar.setStringPainted(true);
                                                progressBar.setString(chef.getProgressoAtual() + "%");
                                                progressBar.setPreferredSize(new Dimension(150, 20));
                                                progressBar.setForeground(chef.getStatus() == Chef.Status.FINALIZANDO ? 
                                                    COR_SECUNDARIA : COR_PRIMARIA);
                                                
                                                JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                                                progressPanel.setOpaque(false);
                                                progressPanel.add(progressBar);
                                                
                                                card.add(nome);
                                                card.add(Box.createRigidArea(new Dimension(0, 5)));
                                                card.add(hab);
                                                card.add(Box.createRigidArea(new Dimension(0, 5)));
                                                card.add(status);
                                                card.add(Box.createRigidArea(new Dimension(0, 5)));
                                                card.add(progressPanel);
                                            } else {
                                                card.add(nome);
                                                card.add(Box.createRigidArea(new Dimension(0, 5)));
                                                card.add(hab);
                                                card.add(Box.createRigidArea(new Dimension(0, 5)));
                                                card.add(status);
                                            }
                                            
                                            // Adiciona informação de utensílios em uso, se houver
                                            if (!chef.getUtensiliosEmUso().isEmpty()) {
                                                JLabel utensLabel = new JLabel(" Utensílios em uso:");
                                                utensLabel.setFont(new Font("Arial", Font.BOLD, 13));
                                                utensLabel.setForeground(COR_PRIMARIA);
                                                card.add(Box.createRigidArea(new Dimension(0, 8)));
                                                card.add(utensLabel);
                                                
                                                for (Utensilio u : chef.getUtensiliosEmUso()) {
                                                    JLabel utensilioLabel = new JLabel("  • " + getIconeUtensilio(u) + " " + u.toString());
                                                    utensilioLabel.setFont(new Font("Arial", Font.PLAIN, 13));
                                                    card.add(utensilioLabel);
                                                }
                                            }
                                            
                                            // Adiciona informação sobre o pedido atual, se estiver preparando
                                            if (chef.getReceitaAtual() != null) {
                                                JPanel infoPanel = new JPanel();
                                                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                                                infoPanel.setOpaque(false);

                                                JLabel receitaLabel = new JLabel("\uD83D\uDC69\u200D\uD83C\uDF73 Preparando:", SwingConstants.LEFT);
                                                receitaLabel.setFont(new Font("Arial", Font.BOLD, 12));
                                                receitaLabel.setForeground(COR_PRIMARIA);
                                                
                                                JLabel nomeReceita = new JLabel(chef.getReceitaAtual().getNome(), SwingConstants.LEFT);
                                                nomeReceita.setFont(new Font("Arial", Font.ITALIC, 11));
                                                nomeReceita.setForeground(new Color(70, 130, 180));
                                                
                                                infoPanel.add(receitaLabel);
                                                infoPanel.add(nomeReceita);
                                                card.add(Box.createRigidArea(new Dimension(0, 5)));
                                                card.add(infoPanel);
                                            }
                                            
                                            panel.add(card);
                                            panel.add(Box.createRigidArea(new Dimension(0, 10)));
                                        }
                                        
                                        panel.revalidate();
                                        panel.repaint();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Atualiza painéis
            utensiliosPanel.revalidate();
            utensiliosPanel.repaint();
            cozinhaPanel.revalidate();
            cozinhaPanel.repaint();
            cozinhaLayeredPane.revalidate();
            cozinhaLayeredPane.repaint();
            
        } catch (Exception e) {
            // Em caso de erro na atualização, registrar no log
            System.err.println("Erro ao atualizar painel de cozinha: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void aplicarTema() {

        UIManager.put("Label.foreground", COR_TEXTO);
        UIManager.put("Button.foreground", COR_TEXTO); 
        UIManager.put("CheckBox.foreground", COR_TEXTO);
        UIManager.put("RadioButton.foreground", COR_TEXTO);
        UIManager.put("Panel.foreground", COR_TEXTO);
        UIManager.put("TitledBorder.titleColor", COR_TEXTO);
        

        UIManager.put("ComboBox.foreground", COR_TEXTO);
        UIManager.put("ComboBox.background", COR_PANEL);
        UIManager.put("ComboBox.selectionBackground", COR_PRIMARIA);

        
        UIManager.put("ProgressBar.foreground", COR_SECUNDARIA); // Cor da barra de progresso
        UIManager.put("TextArea.foreground", COR_TEXTO);
        UIManager.put("TextField.foreground", COR_TEXTO);

        UIManager.put("PasswordField.foreground", COR_TEXTO);
        // UIManager.put("PasswordField.background", textBg); // Deixa o seu ajuste manual dominar
        UIManager.put("TextPane.foreground", COR_TEXTO);

        UIManager.put("EditorPane.foreground", COR_TEXTO);
        if (mainPanel != null) mainPanel.setBackground(COR_FUNDO);
        if (cozinhaPanel != null) cozinhaPanel.setBackground(COR_FUNDO);
        if (pedidosPanel != null) {
            pedidosPanel.setBackground(COR_PANEL);
        }
        if (logArea != null) {
            logArea.setBackground(COR_LOG_BG);
            logArea.setForeground(COR_LOG_TXT);
            logArea.setFont(new Font("Arial", Font.PLAIN, 14));
        }
        if (utensiliosPanel != null) {
            utensiliosPanel.setBackground(COR_PANEL);
        }
        if (eventosPanel != null) {
            eventosPanel.setBackground(COR_PANEL);
        }
        
        atualizarCoresComponentes(mainPanel); 
        
        if (frame != null) {
            SwingUtilities.updateComponentTreeUI(frame);
        }
    }

    // Atualiza recursivamente a cor de texto dos componentes
    private void atualizarCoresComponentes(Container container) {
        if (container == null) return;
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                if (label.getText() != null && !label.getText().trim().isEmpty() && label.getIcon() == null) {
                    label.setForeground(COR_TEXTO);
                } else if (label.getIcon() != null && label.getText() != null && !label.getText().trim().isEmpty()) {
                    label.setForeground(COR_TEXTO);
                }
            } else if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getBackground() == COR_PRIMARIA || button.getBackground() == COR_SECUNDARIA || button.getBackground() == COR_DESTAQUE){
                     button.setForeground(Color.BLACK);
                } else {
                     button.setForeground(COR_TEXTO);
                }
            } else if (comp instanceof JCheckBox || comp instanceof JRadioButton) {
                 comp.setForeground(COR_TEXTO);
            } else if (comp instanceof JComboBox) {
                // Cor do texto principal já definida pelo UIManager
            } else if (comp instanceof JProgressBar) {
                JProgressBar progressBar = (JProgressBar) comp;
                progressBar.setStringPainted(true);
                progressBar.setForeground(Color.GREEN); 
            } else if (comp instanceof JTextArea) {
                if (comp == logArea) {
                    ((JTextArea) comp).setBackground(COR_LOG_BG);
                    ((JTextArea) comp).setForeground(COR_LOG_TXT);
                } else {
                    comp.setForeground(COR_TEXTO);

            }} else if (comp instanceof JTextField || comp instanceof JPasswordField) {
                comp.setForeground(COR_TEXTO);

            } else if (comp instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) comp;
                atualizarCoresComponentes(scroll.getViewport());
            } else if (comp instanceof JPanel) {
                atualizarCoresComponentes((Container) comp);
            }
        }

        if (container instanceof JPanel) {
            JPanel panelContainer = (JPanel) container;
            Border border = panelContainer.getBorder();
            if (border instanceof TitledBorder) {
                TitledBorder titledBorder = (TitledBorder) border;
                titledBorder.setTitleColor(COR_TEXTO);
            } 
        }
    }

    public static void atualizarPainelCozinhaStatic() {
        if (instanciaAtual != null) instanciaAtual.atualizarPainelCozinha();
    }
    public static void atualizarPainelPedidosStatic() {
        if (instanciaAtual != null) instanciaAtual.atualizarPainelPedidos();
    }

    // Método para verificar e corrigir problemas de layout
    private void verificarECorrigirLayout() {
        System.out.println(" Verificando layout para notebook...");
        
        // Verifica se os componentes principais estão presentes
        boolean temHeader = false;
        boolean temPainelCentral = false;
        boolean temLog = false;
        
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof JPanel && comp == mainPanel.getComponent(0)) {
                temHeader = true;
                System.out.println(" Header encontrado");
            }
            else if (comp instanceof JPanel && comp != mainPanel.getComponent(0) && comp != mainPanel.getComponent(2)) {
                temPainelCentral = true;
                System.out.println(" Painel central encontrado");
                
                // Verifica os painéis dentro do painel central
                JPanel centroPainel = (JPanel) comp;
                boolean temPainelPedidos = false;
                boolean temPainelCozinha = false;
                boolean temPainelChefs = false;
                
                for (Component centroComp : centroPainel.getComponents()) {
                    if (centroComp instanceof JPanel) {
                        JPanel subPainel = (JPanel) centroComp;
                        if (subPainel.getBorder() instanceof TitledBorder) {
                            TitledBorder border = (TitledBorder) subPainel.getBorder();
                            String titulo = border.getTitle();
                            
                            if (titulo.contains("Pedidos")) {
                                temPainelPedidos = true;
                                System.out.println(" Painel de pedidos encontrado");
                            } else if (titulo.contains("Chefs")) {
                                temPainelChefs = true;
                                System.out.println(" Painel de chefs encontrado");
                            }
                        }
                    } else if (centroComp instanceof JPanel) {
                        // Pode ser o painel da cozinha
                        for (Component c : ((JPanel) centroComp).getComponents()) {
                            if (c instanceof JPanel && ((JPanel) c).getBorder() instanceof TitledBorder) {
                                TitledBorder border = (TitledBorder) ((JPanel) c).getBorder();
                                if (border.getTitle().contains("Cozinha")) {
                                    temPainelCozinha = true;
                                    System.out.println(" Painel de cozinha encontrado");
                                }
                            }
                        }
                    }
                }
                
                // Se estiver faltando algum painel importante, recria a interface
                if (!temPainelPedidos || !temPainelCozinha || !temPainelChefs) {
                    System.out.println(" Painéis principais faltando! Recriando interface...");
                    configurarInterface();
                    return;
                }
            }
            else if (comp instanceof JScrollPane) {
                temLog = true;
                System.out.println(" Log encontrado");
            }
        }
        
        // Se está faltando componentes básicos, recria a interface
        if (!temHeader || !temPainelCentral || !temLog) {
            System.out.println(" Componentes principais faltando! Recriando interface...");
            configurarInterface();
            return;
        }
        
        // Força repaint para garantir que todos os componentes estão visíveis
        SwingUtilities.updateComponentTreeUI(frame);
        
        // Define tamanho mínimo para a janela para garantir visibilidade
        if (frame.getWidth() < 1280 || frame.getHeight() < 800) {
            frame.setMinimumSize(new Dimension(1280, 800));
            frame.setSize(new Dimension(1280, 800));
        }
        
        System.out.println(" Verificação de layout concluída");
    }
}

