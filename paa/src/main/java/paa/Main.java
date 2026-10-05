package paa;

/**
 * Ponto de entrada dos experimentos: busca-m, comparacao, crescimento, tudo ou estouro.
 *
 * <p>Os experimentos rodam numa thread com pilha grande: com pivô no primeiro
 * elemento, vetores ordenados e inversos de 100k chegam a profundidade de recursão
 * 100k, que estoura a pilha padrão da JVM. O "estouro" roda de propósito na pilha
 * padrão, para registrar em que n ela estoura.
 */
public class Main {

    static final long PILHA_BYTES = 1L << 30;

    public static void main(String[] args) throws InterruptedException {
        String experimento = args.length > 0 ? args[0] : "";
        Runnable tarefa;
        switch (experimento) {
            case "busca-m":
                tarefa = () -> Experimentos.buscaM(PILHA_BYTES);
                break;
            case "comparacao":
                tarefa = () -> Experimentos.comparacao(PILHA_BYTES);
                break;
            case "crescimento":
                tarefa = () -> Experimentos.crescimento(PILHA_BYTES);
                break;
            case "tudo":
                tarefa = () -> {
                    Experimentos.buscaM(PILHA_BYTES);
                    Experimentos.comparacao(PILHA_BYTES);
                    Experimentos.crescimento(PILHA_BYTES);
                };
                break;
            case "estouro":
                Experimentos.estouro();
                return;
            default:
                System.err.println("uso: paa.Main busca-m | comparacao | crescimento | tudo | estouro");
                System.exit(2);
                return;
        }

        boolean[] falhou = {false};
        Thread thread = new Thread(null, tarefa, "experimentos", PILHA_BYTES);
        thread.setUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            falhou[0] = true;
        });
        thread.start();
        thread.join();
        if (falhou[0]) {
            System.exit(1);
        }
    }
}
