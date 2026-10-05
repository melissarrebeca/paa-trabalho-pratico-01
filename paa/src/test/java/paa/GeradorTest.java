package paa;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class GeradorTest {

    private static final int N = 10_000;

    @Test
    void mesmaSementeGeraAMesmaMassa() {
        assertArrayEquals(Gerador.aleatorio(N, 7), Gerador.aleatorio(N, 7));
        assertArrayEquals(Gerador.repetidos(N, 7), Gerador.repetidos(N, 7));
    }

    @Test
    void sementesDiferentesGeramMassasDiferentes() {
        assertFalse(Arrays.equals(Gerador.aleatorio(N, 7), Gerador.aleatorio(N, 8)));
    }

    @Test
    void cadaTipoDeVetorTemOTamanhoPedido() {
        assertEquals(N, Gerador.aleatorio(N, 1).length);
        assertEquals(N, Gerador.ordenado(N).length);
        assertEquals(N, Gerador.inverso(N).length);
        assertEquals(N, Gerador.repetidos(N, 1).length);
        assertEquals(0, Gerador.aleatorio(0, 1).length);
    }

    @Test
    void ordenadoEhEstritamenteCrescente() {
        int[] v = Gerador.ordenado(N);
        for (int i = 1; i < N; i++) {
            assertTrue(v[i - 1] < v[i]);
        }
    }

    @Test
    void inversoEhEstritamenteDecrescente() {
        int[] v = Gerador.inverso(N);
        for (int i = 1; i < N; i++) {
            assertTrue(v[i - 1] > v[i]);
        }
    }

    @Test
    void repetidosTemValoresEntreZeroENSobreCem() {
        int[] v = Gerador.repetidos(N, 1);
        for (int x : v) {
            assertTrue(x >= 0 && x <= N / 100);
        }
        long distintos = Arrays.stream(v).distinct().count();
        assertTrue(distintos <= N / 100 + 1);
    }

    @Test
    void aleatorioNaoVemOrdenado() {
        int[] v = Gerador.aleatorio(N, 1);
        int[] ordenado = v.clone();
        Arrays.sort(ordenado);
        assertFalse(Arrays.equals(v, ordenado));
    }
}
