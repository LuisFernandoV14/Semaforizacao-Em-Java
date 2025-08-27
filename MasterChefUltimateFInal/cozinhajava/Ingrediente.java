public enum Ingrediente {
    OVOS("🥚", 5),
    LEITE("🥛", 3),
    CARNE("🥩", 4),
    FRANGO("🍗", 4),
    TOMATE("🍅", 2),
    ALFACE("🥬", 1),
    QUEIJO("🧀", 5),
    ARROZ("🍚", 10),
    MASSA("🍝", 8),
    PEIXE("🐟", 2),
    CAMARAO("🦐", 2),
    COGUMELOS("🍄", 3);

    private final String icone;
    private final int diasValidade;
    private int qualidade;
    private boolean refrigerado;

    Ingrediente(String icone, int diasValidade) {
        this.icone = icone;
        this.diasValidade = diasValidade;
        this.qualidade = 100;
        this.refrigerado = false;
    }

    public String getIcone() {
        return icone;
    }

    public int getDiasValidade() {
        return diasValidade;
    }

    public int getQualidade() {
        return qualidade;
    }

    public void setQualidade(int qualidade) {
        this.qualidade = Math.max(0, Math.min(100, qualidade));
    }

    public boolean isRefrigerado() {
        return refrigerado;
    }

    public void setRefrigerado(boolean refrigerado) {
        this.refrigerado = refrigerado;
    }

    public void deteriorar() {
        if (!refrigerado) {
            qualidade -= 10;
        } else {
            qualidade -= 2;
        }
        qualidade = Math.max(0, qualidade);
    }

    public boolean estaEstragado() {
        return qualidade < 30;
    }

    public String getQualidadeDescricao() {
        if (qualidade >= 90) return "Excelente ⭐";
        if (qualidade >= 70) return "Bom 👍";
        if (qualidade >= 50) return "Regular 😐";
        if (qualidade >= 30) return "Ruim 😟";
        return "Estragado ⚠️";
    }
} 