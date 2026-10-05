package paa;

public class InsertionSort {

    private InsertionSort() {
    }

    public static void ordenar(int[] v, Contador c) {
        for (int i = 1; i < v.length; i++) {
            int x = v[i];
            int j = i - 1;
            while (j >= 0 && Particao.menor(x, v[j], c)) {
                v[j + 1] = v[j];
                c.deslocamentos++;
                j--;
            }
            v[j + 1] = x;
        }
    }
}
