# Estudo comparativo do Quicksort
> Integrantes: Bruna de Lima Furtado e Melissa Rebeca de Souza Araujo

Link para acesso ao Overleaf: https://www.overleaf.com/read/yhccndbckxzp#a79dbd

Trabalho prático de Projeto e Análise de Algoritmos (PUC Minas). O trabalho compara experimentalmente três versões do Quicksort em tempo de execução, comparações, trocas e deslocamentos:

- **a) Quicksort recursivo**: partição de Hoare/Ziviani com pivô no primeiro elemento.
- **b) Quicksort híbrido**: não particiona subvetores com menos de **M** elementos e termina com uma única passada de Insertion Sort.
- **c) Quicksort híbrido com mediana-de-três**: o híbrido com o pivô escolhido entre o primeiro, o central e o último elemento.

### Testes

Os testes conferem três coisas:
- **Corretude:** cada versão, em cada tipo de vetor, termina igual ao `Arrays.sort`.
- **Casos de borda:** vetor vazio, 1 e 2 elementos, todos iguais, e tamanhos em volta de M.
- **Regra dos contadores:** por exemplo, o Insertion Sort em vetor ordenado faz exatamente n − 1 comparações.

### Experimentos

| Experimento | O que faz |
|---|---|
| `busca-m` | Roda as duas versões híbridas para M ∈ {2, 5, 10, 15, 20, 25, 30, 50, 100} em vetores aleatórios de 1000 elementos (100 repetições por M) e valida em 100.000. Imprime o M de menor tempo médio de cada versão. |
| `comparacao` | Roda 3 versões × 4 tipos de vetor (aleatório, ordenado, inverso, repetidos) × 3 tamanhos (1k, 10k, 100k) × 10 repetições. |
| `crescimento` | Roda o Quicksort recursivo em vetores ordenados com n = 1k, 2k, 4k, …, 32k (pior caso). |
| `estouro` | Roda o pior caso na pilha padrão até o `StackOverflowError`, como evidência da profundidade de recursão O(n). |

## Como funciona a medição

- Mesma entrada para todas as versões: cada tipo de vetor e tamanho tem uma semente fixa, e cada execução recebe uma cópia fresca da massa de teste.
- Só a ordenação é cronometrada, com `System.nanoTime()`. Geração e cópia ficam fora da medição.
- Repetições: são 10 por massa de teste na comparação geral e no crescimento, e 100 na busca de M: uma ordenação de 1000 elementos leva cerca de 40 µs, e com 10 repetições a escolha do M mudava de uma rodada para outra. Os contadores são determinísticos e ficam iguais em todas as repetições; a repetição serve para tirar média e desvio padrão do tempo.
- Pilha grande: os experimentos rodam numa thread com pilha de 1 GB. Com o pivô no primeiro elemento, vetores ordenados e inversos de 100.000 elementos chegam a profundidade de recursão 100.000.

## Estrutura

```
src/main/java/paa/
    Contador.java                  comparações, trocas e deslocamentos
    Particao.java                  partição de Hoare/Ziviani compartilhada
    InsertionSort.java
    QuickSortRecursivo.java        versão a
    QuickSortHibrido.java          versão b
    QuickSortHibridoMediana.java   versão c
    Gerador.java                   massas de teste
    TipoVetor.java                 tipos de vetor e sementes
    Experimentos.java              busca de M, comparação geral, crescimento, estouro
    Ambiente.java                  descrição da máquina no topo dos CSVs
    Main.java                      ponto de entrada
src/test/java/paa/                 testes JUnit 5
scripts/graficos.py                gráficos e tabelas
```

