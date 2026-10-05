package paa;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

/**
 * Os três experimentos do trabalho (busca de M, comparação geral e crescimento),
 * cada um gravando um CSV em resultados/, e a observação do estouro de pilha.
 */
public class Experimentos {

    /** M de cada versão híbrida, definido pela busca de M. Atualize após a rodada final da busca. */
    static final int M_HIBRIDO = 10;
    static final int M_MEDIANA = 15;

    static final int AQUECIMENTO = 3;
    static final int RODADAS_AQUECIMENTO_JVM = 3000;
    static final int REPETICOES = 10;
    /** Na busca de M, cada ordenação de 1000 elementos leva ~40 µs: com 10 repetições a média oscila demais. */
    static final int REPETICOES_BUSCA_M = 100;

    static final int[] VALORES_M = {2, 5, 10, 15, 20, 25, 30, 50, 100};
    static final int TAMANHO_BUSCA_M = 1000;
    static final int TAMANHO_VALIDACAO_M = 100_000;
    static final int[] TAMANHOS = {1000, 10_000, 100_000};
    static final int[] TAMANHOS_CRESCIMENTO = {1000, 2000, 4000, 8000, 16_000, 32_000};

    private static final Path PASTA = Path.of("resultados");

    enum Versao {
        RECURSIVO("recursivo"),
        HIBRIDO("hibrido"),
        HIBRIDO_MEDIANA("hibrido_mediana");

        final String nome;

        Versao(String nome) {
            this.nome = nome;
        }

        /** O Quicksort recursivo ignora M. */
        void ordenar(int[] v, int m, Contador c) {
            switch (this) {
                case RECURSIVO:
                    QuickSortRecursivo.ordenar(v, c);
                    break;
                case HIBRIDO:
                    QuickSortHibrido.ordenar(v, m, c);
                    break;
                default:
                    QuickSortHibridoMediana.ordenar(v, m, c);
            }
        }

        int mPadrao() {
            switch (this) {
                case HIBRIDO:
                    return M_HIBRIDO;
                case HIBRIDO_MEDIANA:
                    return M_MEDIANA;
                default:
                    return 1;
            }
        }
    }

    /** Recebe o resultado de cada repetição medida de um bloco. */
    private interface Registro {
        void execucao(int repeticao, double tempoMs, Contador c);
    }

    private Experimentos() {
    }

    static void buscaM(long pilhaBytes) {
        aquecerJvm();
        try (PrintWriter csv = abrir("busca_m.csv", pilhaBytes)) {
            csv.println("versao,tamanho,m,repeticao,tempo_ms,comparacoes,trocas,deslocamentos");
            for (int n : new int[] {TAMANHO_BUSCA_M, TAMANHO_VALIDACAO_M}) {
                int[] massa = TipoVetor.ALEATORIO.gerar(n);
                for (Versao versao : new Versao[] {Versao.HIBRIDO, Versao.HIBRIDO_MEDIANA}) {
                    int melhorM = 0;
                    double melhorTempo = Double.MAX_VALUE;
                    for (int m : VALORES_M) {
                        double media = bloco(versao, massa, m, REPETICOES_BUSCA_M, (rep, ms, c) ->
                                csv.printf(Locale.ROOT, "%s,%d,%d,%d,%.6f,%d,%d,%d%n", versao.nome, n, m, rep,
                                        ms, c.comparacoes, c.trocas, c.deslocamentos));
                        System.out.printf(Locale.ROOT, "busca de M  %-16s n=%-6d M=%-3d  %9.4f ms%n",
                                versao.nome, n, m, media);
                        if (media < melhorTempo) {
                            melhorTempo = media;
                            melhorM = m;
                        }
                    }
                    System.out.printf("  -> menor tempo médio de %s com n=%d: M=%d%n", versao.nome, n, melhorM);
                }
            }
        }
    }

    static void comparacao(long pilhaBytes) {
        aquecerJvm();
        try (PrintWriter csv = abrir("comparacao.csv", pilhaBytes)) {
            csv.println("versao,tipo_vetor,tamanho,m,repeticao,tempo_ms,comparacoes,trocas,deslocamentos");
            for (int n : TAMANHOS) {
                for (TipoVetor tipo : TipoVetor.values()) {
                    int[] massa = tipo.gerar(n);
                    for (Versao versao : Versao.values()) {
                        int m = versao.mPadrao();
                        String colunaM = versao == Versao.RECURSIVO ? "" : String.valueOf(m);
                        double media = bloco(versao, massa, m, REPETICOES, (rep, ms, c) ->
                                csv.printf(Locale.ROOT, "%s,%s,%d,%s,%d,%.6f,%d,%d,%d%n", versao.nome, tipo.nome, n,
                                        colunaM, rep, ms, c.comparacoes, c.trocas, c.deslocamentos));
                        System.out.printf(Locale.ROOT, "comparacao  %-16s %-10s n=%-6d %12.4f ms%n",
                                versao.nome, tipo.nome, n, media);
                    }
                }
            }
        }
    }

    static void crescimento(long pilhaBytes) {
        aquecerJvm();
        try (PrintWriter csv = abrir("crescimento.csv", pilhaBytes)) {
            csv.println("tamanho,repeticao,tempo_ms,comparacoes,trocas");
            for (int n : TAMANHOS_CRESCIMENTO) {
                int[] massa = TipoVetor.ORDENADO.gerar(n);
                double media = bloco(Versao.RECURSIVO, massa, 1, REPETICOES, (rep, ms, c) ->
                        csv.printf(Locale.ROOT, "%d,%d,%.6f,%d,%d%n", n, rep, ms, c.comparacoes, c.trocas));
                System.out.printf(Locale.ROOT, "crescimento recursivo ordenado n=%-6d %10.4f ms%n", n, media);
            }
        }
    }

    /**
     * Ordena vetores ordenados de tamanho crescente com o Quicksort recursivo na pilha
     * em que for chamado, até o StackOverflowError. Deve rodar na pilha padrão da JVM.
     */
    static void estouro() {
        for (int n = 1000; n <= 1_000_000; n += 1000) {
            try {
                QuickSortRecursivo.ordenar(Gerador.ordenado(n), new Contador());
            } catch (StackOverflowError e) {
                System.out.printf("StackOverflowError com a pilha padrão em n=%d (último sem estouro: n=%d)%n",
                        n, n - 1000);
                return;
            }
        }
        System.out.println("Nenhum estouro até n=1.000.000");
    }

    /**
     * Ordena milhares de vetores de 1000 elementos com todas as versões e vários M antes
     * de qualquer medição, para que o JIT já tenha compilado o código. Sem isso, a busca
     * de M mede os primeiros valores de M com código ainda interpretado e a curva entorta.
     */
    private static void aquecerJvm() {
        System.out.println("aquecendo a JVM...");
        int[] massa = TipoVetor.ALEATORIO.gerar(1000);
        Contador c = new Contador();
        for (int rodada = 0; rodada < RODADAS_AQUECIMENTO_JVM; rodada++) {
            int m = VALORES_M[rodada % VALORES_M.length];
            for (Versao versao : Versao.values()) {
                medir(versao, massa, m, c);
            }
        }
    }

    /**
     * Aquece com {@link #AQUECIMENTO} execuções descartadas, mede {@code repeticoes}
     * execuções sobre cópias frescas da massa e devolve o tempo médio em ms.
     */
    private static double bloco(Versao versao, int[] massa, int m, int repeticoes, Registro registro) {
        Contador c = new Contador();
        for (int i = 0; i < AQUECIMENTO; i++) {
            medir(versao, massa, m, c);
        }
        double soma = 0;
        for (int rep = 1; rep <= repeticoes; rep++) {
            double ms = medir(versao, massa, m, c);
            registro.execucao(rep, ms, c);
            soma += ms;
        }
        return soma / repeticoes;
    }

    /** Mede só a ordenação: a cópia e o reset ficam fora do cronômetro. */
    private static double medir(Versao versao, int[] massa, int m, Contador c) {
        int[] copia = Arrays.copyOf(massa, massa.length);
        c.reset();
        long inicio = System.nanoTime();
        versao.ordenar(copia, m, c);
        long fim = System.nanoTime();
        return (fim - inicio) / 1_000_000.0;
    }

    private static PrintWriter abrir(String arquivo, long pilhaBytes) {
        try {
            Files.createDirectories(PASTA);
            PrintWriter csv = new PrintWriter(Files.newBufferedWriter(PASTA.resolve(arquivo)));
            for (String linha : Ambiente.descrever(pilhaBytes)) {
                csv.println("# " + linha);
            }
            return csv;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
