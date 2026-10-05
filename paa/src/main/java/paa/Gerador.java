package paa;

import java.util.Random;

/** Massas de teste determinísticas: a mesma semente gera sempre o mesmo vetor. */
public class Gerador {

    private Gerador() {
    }

    public static int[] aleatorio(int n, long semente) {
        Random rnd = new Random(semente);
        int[] v = new int[n];
        for (int i = 0; i < n; i++) {
            v[i] = rnd.nextInt();
        }
        return v;
    }

    public static int[] ordenado(int n) {
        int[] v = new int[n];
        for (int i = 0; i < n; i++) {
            v[i] = i;
        }
        return v;
    }

    public static int[] inverso(int n) {
        int[] v = new int[n];
        for (int i = 0; i < n; i++) {
            v[i] = n - 1 - i;
        }
        return v;
    }

    /** Valores em 0..n/100, para garantir muitas chaves iguais. */
    public static int[] repetidos(int n, long semente) {
        Random rnd = new Random(semente);
        int[] v = new int[n];
        for (int i = 0; i < n; i++) {
            v[i] = rnd.nextInt(n / 100 + 1);
        }
        return v;
    }
}
