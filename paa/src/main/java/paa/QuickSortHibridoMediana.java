package paa;

/**
 * Versão c: o Quicksort híbrido em que o pivô é a mediana entre o primeiro, o
 * central e o último elemento do subvetor.
 */
public class QuickSortHibridoMediana {

    private QuickSortHibridoMediana() {
    }

    public static void ordenar(int[] v, int m, Contador c) {
        if (m < 1) {
            throw new IllegalArgumentException("M deve ser >= 1, recebido " + m);
        }
        if (v.length > 1) {
            qsort(v, 0, v.length - 1, m, c);
        }
        InsertionSort.ordenar(v, c);
    }

    private static void qsort(int[] v, int esq, int dir, int m, Contador c) {
        if (dir - esq + 1 < m) {
            return; // menos de M elementos: fica para o Insertion Sort
        }
        int[] ij = Particao.particionar(v, esq, dir, medianaDeTres(v, esq, dir, c), c);
        int i = ij[0];
        int j = ij[1];
        if (esq < j) {
            qsort(v, esq, j, m, c);
        }
        if (i < dir) {
            qsort(v, i, dir, m, c);
        }
    }

    /** Ordena v[esq], v[meio] e v[dir] entre si, nas próprias posições, e devolve v[meio]. */
    private static int medianaDeTres(int[] v, int esq, int dir, Contador c) {
        int meio = (esq + dir) / 2;
        if (Particao.menor(v[meio], v[esq], c)) {
            Particao.troca(v, esq, meio, c);
        }
        if (Particao.menor(v[dir], v[esq], c)) {
            Particao.troca(v, esq, dir, c);
        }
        if (Particao.menor(v[dir], v[meio], c)) {
            Particao.troca(v, meio, dir, c);
        }
        return v[meio];
    }
}
