package paa;

/**
 * Partição de Hoare/Ziviani, compartilhada pelas três versões do Quicksort.
 * Recebe o valor do pivô: quem escolhe o pivô é a versão.
 */
final class Particao {

    private Particao() {
    }

    /**
     * Particiona v[esq..dir] em torno de {@code pivo}, que precisa ser um dos
     * elementos da faixa. Devolve {i, j}: ao final, v[esq..j] <= pivo <= v[i..dir].
     */
    static int[] particionar(int[] v, int esq, int dir, int pivo, Contador c) {
        int i = esq;
        int j = dir;
        do {
            while (menor(v[i], pivo, c)) {
                i++;
            }
            while (menor(pivo, v[j], c)) {
                j--;
            }
            if (i <= j) {
                troca(v, i, j, c);
                i++;
                j--;
            }
        } while (i <= j);
        return new int[] {i, j};
    }

    static boolean menor(int a, int b, Contador c) {
        c.comparacoes++;
        return a < b;
    }

    static void troca(int[] v, int i, int j, Contador c) {
        int aux = v[i];
        v[i] = v[j];
        v[j] = aux;
        c.trocas++;
    }
}
