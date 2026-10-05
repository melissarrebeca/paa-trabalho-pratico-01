package paa;

/**
 * Totais de trabalho de uma ordenação. É um objeto para ser atualizado dentro
 * da recursão sem precisar ser devolvido. Regra de contagem em CONTEXT.md.
 */
public class Contador {
    public long comparacoes = 0;
    public long trocas = 0;
    public long deslocamentos = 0;

    public void reset() {
        comparacoes = 0;
        trocas = 0;
        deslocamentos = 0;
    }
}
