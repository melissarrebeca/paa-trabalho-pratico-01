package paa;

/** Versão a: pivô no primeiro elemento (ADR 0001), recursão até subvetores de tamanho 1. */
public class QuickSortRecursivo {

    private QuickSortRecursivo() {
    }

    public static void ordenar(int[] v, Contador c) {
        if (v.length > 1) {
            qsort(v, 0, v.length - 1, c);
        }
    }

    private static void qsort(int[] v, int esq, int dir, Contador c) {
        int[] ij = Particao.particionar(v, esq, dir, v[esq], c);
        int i = ij[0];
        int j = ij[1];
        if (esq < j) {
            qsort(v, esq, j, c);
        }
        if (i < dir) {
            qsort(v, i, dir, c);
        }
    }
}
