package paa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Invariantes da regra dos contadores (ver CONTEXT.md). */
class ContadoresTest {

    private static final int N = 1000;

    @Test
    void insertionSortEmVetorOrdenadoFazNMenosUmComparacoesESemDeslocamentos() {
        Contador c = new Contador();

        InsertionSort.ordenar(Gerador.ordenado(N), c);

        assertEquals(N - 1, c.comparacoes);
        assertEquals(0, c.deslocamentos);
        assertEquals(0, c.trocas);
    }

    @Test
    void insertionSortEmVetorInversoDeslocaTodosOsPares() {
        Contador c = new Contador();

        InsertionSort.ordenar(Gerador.inverso(N), c);

        long pares = (long) N * (N - 1) / 2;
        assertEquals(pares, c.comparacoes);
        assertEquals(pares, c.deslocamentos);
        assertEquals(0, c.trocas);
    }

    @Test
    void quicksortRecursivoNaoDesloca() {
        Contador c = new Contador();

        QuickSortRecursivo.ordenar(Gerador.aleatorio(N, 42), c);

        assertEquals(0, c.deslocamentos);
    }

    @Test
    void hibridoComMUmContaOMesmoQueORecursivoMaisAPassadaFinal() {
        for (int[] massa : OrdenacaoTest.massas(N)) {
            Contador recursivo = new Contador();
            Contador hibrido = new Contador();

            QuickSortRecursivo.ordenar(massa.clone(), recursivo);
            QuickSortHibrido.ordenar(massa.clone(), 1, hibrido);

            // a passada final do Insertion Sort encontra o vetor já ordenado
            assertEquals(recursivo.comparacoes + (N - 1), hibrido.comparacoes);
            assertEquals(recursivo.trocas, hibrido.trocas);
            assertEquals(0, hibrido.deslocamentos);
        }
    }

    @Test
    void hibridoComMDoisEquivaleAMUm() {
        int[] massa = Gerador.aleatorio(N, 42);
        Contador m1 = new Contador();
        Contador m2 = new Contador();

        QuickSortHibrido.ordenar(massa.clone(), 1, m1);
        QuickSortHibrido.ordenar(massa.clone(), 2, m2);

        assertEquals(m1.comparacoes, m2.comparacoes);
        assertEquals(m1.trocas, m2.trocas);
        assertEquals(m1.deslocamentos, m2.deslocamentos);
    }

    @Test
    void quicksortRecursivoEmVetorOrdenadoEhQuadratico() {
        Contador c = new Contador();

        QuickSortRecursivo.ordenar(Gerador.ordenado(N), c);

        long n2 = (long) N * N;
        assertTrue(c.comparacoes >= n2 / 4 && c.comparacoes <= n2,
                "esperado entre n²/4 e n², obtido " + c.comparacoes);
    }

    @Test
    void quicksortRecursivoEmVetorAleatorioEhNLogN() {
        Contador c = new Contador();

        QuickSortRecursivo.ordenar(Gerador.aleatorio(N, 42), c);

        double nLogN = N * (Math.log(N) / Math.log(2));
        assertTrue(c.comparacoes < 3 * nLogN,
                "esperado da ordem de n log n, obtido " + c.comparacoes);
    }

    /** A mediana-de-três elimina o pior caso do pivô no primeiro elemento. */
    @Test
    void medianaDeTresEmVetorOrdenadoOuInversoEhNLogN() {
        double nLogN = N * (Math.log(N) / Math.log(2));
        for (int[] massa : new int[][] {Gerador.ordenado(N), Gerador.inverso(N)}) {
            Contador c = new Contador();

            QuickSortHibridoMediana.ordenar(massa, 1, c);

            assertTrue(c.comparacoes < 3 * nLogN,
                    "esperado da ordem de n log n, obtido " + c.comparacoes);
        }
    }

    @Test
    void resetZeraOsTresContadores() {
        Contador c = new Contador();
        InsertionSort.ordenar(Gerador.inverso(10), c);

        c.reset();

        assertEquals(0, c.comparacoes);
        assertEquals(0, c.trocas);
        assertEquals(0, c.deslocamentos);
    }
}
