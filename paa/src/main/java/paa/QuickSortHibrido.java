package paa;

/**
 * Versão b: o Quicksort recursivo que não particiona subvetores com menos de M
 * elementos e termina com uma única passada do Insertion Sort no vetor inteiro.
 */
public class QuickSortHibrido {

    private QuickSortHibrido() {
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
        int[] ij = Particao.particionar(v, esq, dir, v[esq], c);
        int i = ij[0];
        int j = ij[1];
        if (esq < j) {
            qsort(v, esq, j, m, c);
        }
        if (i < dir) {
            qsort(v, i, dir, m, c);
        }
    }
}
