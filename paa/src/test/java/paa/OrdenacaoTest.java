package paa;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Corretude: cada versão termina com o mesmo vetor que Arrays.sort produz. */
class OrdenacaoTest {

    interface Ordenacao {
        void ordenar(int[] v, Contador c);
    }

    private static final long SEMENTE = 42;

    static Stream<Arguments> versoes() {
        return Stream.of(
                Arguments.of("Insertion Sort", (Ordenacao) InsertionSort::ordenar),
                Arguments.of("Quicksort recursivo", (Ordenacao) QuickSortRecursivo::ordenar),
                Arguments.of("Quicksort híbrido M=1", hibrido(1)),
                Arguments.of("Quicksort híbrido M=2", hibrido(2)),
                Arguments.of("Quicksort híbrido M=10", hibrido(10)),
                Arguments.of("Quicksort híbrido M=25", hibrido(25)),
                Arguments.of("Mediana-de-três M=1", mediana(1)),
                Arguments.of("Mediana-de-três M=2", mediana(2)),
                Arguments.of("Mediana-de-três M=10", mediana(10)),
                Arguments.of("Mediana-de-três M=25", mediana(25)));
    }

    static Ordenacao hibrido(int m) {
        return (v, c) -> QuickSortHibrido.ordenar(v, m, c);
    }

    static Ordenacao mediana(int m) {
        return (v, c) -> QuickSortHibridoMediana.ordenar(v, m, c);
    }

    static List<int[]> massas(int n) {
        List<int[]> massas = new ArrayList<>();
        massas.add(Gerador.aleatorio(n, SEMENTE));
        massas.add(Gerador.ordenado(n));
        massas.add(Gerador.inverso(n));
        massas.add(Gerador.repetidos(n, SEMENTE));
        return massas;
    }

    static Stream<Arguments> versoesETamanhos() {
        int[] tamanhos = {0, 1, 2, 3, 10, 100, 1000};
        return versoes().flatMap(versao -> Arrays.stream(tamanhos)
                .mapToObj(n -> Arguments.of(versao.get()[0], versao.get()[1], n)));
    }

    @ParameterizedTest(name = "{0}, n={2}")
    @MethodSource("versoesETamanhos")
    void ordenaTodosOsTiposDeVetor(String nome, Ordenacao ordenacao, int n) {
        for (int[] massa : massas(n)) {
            assertOrdena(ordenacao, massa);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("versoes")
    void ordenaVetorComTodosIguais(String nome, Ordenacao ordenacao) {
        int[] massa = new int[500];
        Arrays.fill(massa, 7);
        assertOrdena(ordenacao, massa);
    }

    /** Tamanhos em volta de M, onde mora o off-by-one do corte. */
    @ParameterizedTest(name = "n={0}")
    @ValueSource(ints = {9, 10, 11})
    void hibridoOrdenaTamanhosEmVoltaDeM(int n) {
        for (int[] massa : massas(n)) {
            assertOrdena(hibrido(10), massa);
            assertOrdena(mediana(10), massa);
        }
    }

    @Test
    void hibridosRejeitamMMenorQueUm() {
        assertThrows(IllegalArgumentException.class,
                () -> QuickSortHibrido.ordenar(new int[] {2, 1}, 0, new Contador()));
        assertThrows(IllegalArgumentException.class,
                () -> QuickSortHibridoMediana.ordenar(new int[] {2, 1}, 0, new Contador()));
    }

    private static void assertOrdena(Ordenacao ordenacao, int[] massa) {
        int[] esperado = Arrays.copyOf(massa, massa.length);
        Arrays.sort(esperado);
        int[] copia = Arrays.copyOf(massa, massa.length);

        ordenacao.ordenar(copia, new Contador());

        assertArrayEquals(esperado, copia);
    }
}
