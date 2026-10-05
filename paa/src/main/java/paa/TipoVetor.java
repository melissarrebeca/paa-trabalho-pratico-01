package paa;

/** A ordem inicial de uma massa de teste. Cada tipo e tamanho tem sua semente fixa. */
public enum TipoVetor {
    ALEATORIO("aleatorio"),
    ORDENADO("ordenado"),
    INVERSO("inverso"),
    REPETIDOS("repetidos");

    private static final long SEMENTE_BASE = 2026;

    public final String nome;

    TipoVetor(String nome) {
        this.nome = nome;
    }

    public int[] gerar(int n) {
        long semente = SEMENTE_BASE + 10L * n + ordinal();
        switch (this) {
            case ALEATORIO:
                return Gerador.aleatorio(n, semente);
            case ORDENADO:
                return Gerador.ordenado(n);
            case INVERSO:
                return Gerador.inverso(n);
            default:
                return Gerador.repetidos(n, semente);
        }
    }
}
