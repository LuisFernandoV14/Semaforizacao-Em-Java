public enum Utensilio {
    FACA("🔪"),
    PANELA("🍳"),
    LIQUIDIFICADOR("🥛"),
    COLHER("🥄"),
    ESPATULA("🍴"),
    FORNO("🔥"),
    BATEDOR("🧇"),
    DESCASCADOR("⚡"),
    RALADOR("🧀"),
    TIGELA("🥣"),
    TABUA("📋");

    private final String icone;

    Utensilio(String icone) {
        this.icone = icone;
    }

    public String getIcone() {
        return icone;
    }
}
