"""Gera os gráficos (PDF) e as tabelas (LaTeX) do relatório a partir dos CSVs dos experimentos.

Uso: python scripts/graficos.py [--resultados resultados] [--saida figuras]
"""

import argparse
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt  # noqa: E402
import numpy as np  # noqa: E402
import pandas as pd  # noqa: E402

# Ordem fixa: cada versão tem sempre a mesma cor e o mesmo marcador em todos os gráficos.
VERSOES = ["recursivo", "hibrido", "hibrido_mediana"]
NOMES_VERSAO = {
    "recursivo": "Quicksort recursivo",
    "hibrido": "Quicksort híbrido",
    "hibrido_mediana": "Quicksort híbrido com mediana-de-três",
}
CORES = {"recursivo": "#2a78d6", "hibrido": "#eb6834", "hibrido_mediana": "#1baf7a"}
MARCADORES = {"recursivo": "o", "hibrido": "s", "hibrido_mediana": "^"}

NOMES_CURTOS = {"recursivo": "Recursivo", "hibrido": "Híbrido", "hibrido_mediana": "Híbrido + mediana"}

TIPOS = ["aleatorio", "ordenado", "inverso", "repetidos"]
NOMES_TIPO = {"aleatorio": "Aleatório", "ordenado": "Ordenado", "inverso": "Inverso", "repetidos": "Repetidos"}

TINTA = "#333333"
GRADE = "#e3e3e3"


def estilo():
    plt.rcParams.update({
        "font.size": 9,
        "axes.edgecolor": GRADE,
        "axes.labelcolor": TINTA,
        "axes.titlesize": 10,
        "axes.titlecolor": TINTA,
        "axes.spines.top": False,
        "axes.spines.right": False,
        "axes.grid": True,
        "axes.axisbelow": True,
        "grid.color": GRADE,
        "grid.linewidth": 0.6,
        "xtick.color": TINTA,
        "ytick.color": TINTA,
        "legend.frameon": False,
        "lines.linewidth": 2,
        "lines.markersize": 5,
        "savefig.bbox": "tight",
    })


def ler(pasta, arquivo):
    return pd.read_csv(pasta / arquivo, comment="#")


def agregar(df, chaves):
    """Média e desvio do tempo; os contadores são determinísticos por massa, então basta o primeiro."""
    contadores = [c for c in ("comparacoes", "trocas", "deslocamentos") if c in df.columns]
    grupos = df.groupby(chaves, sort=False)
    variaveis = grupos[contadores].nunique().max()
    if (variaveis > 1).any():
        raise ValueError(f"contadores variaram entre repetições da mesma massa: {variaveis.to_dict()}")
    return grupos.agg(
        tempo_medio=("tempo_ms", "mean"),
        tempo_desvio=("tempo_ms", "std"),
        **{c: (c, "first") for c in contadores},
    ).reset_index()


# ---------- formatação das tabelas (padrão brasileiro: 1.234,56) ----------

def inteiro(x):
    return f"{int(x):,}".replace(",", ".")


def decimal(x, casas=3):
    return f"{x:,.{casas}f}".replace(",", "X").replace(".", ",").replace("X", ".")


def tabela_tex(caminho, legenda, rotulo, cabecalho, linhas, alinhamento):
    corpo = "\n".join("    " + " & ".join(linha) + r" \\" for linha in linhas)
    caminho.write_text(
        "% Gerado por scripts/graficos.py. Requer \\usepackage{booktabs}.\n"
        "\\begin{table}[htbp]\n  \\centering\n  \\small\n  \\setlength{\\tabcolsep}{4pt}\n"
        f"  \\caption{{{legenda}}}\n  \\label{{{rotulo}}}\n"
        f"  \\begin{{tabular}}{{{alinhamento}}}\n    \\toprule\n"
        f"    {' & '.join(cabecalho)} \\\\\n    \\midrule\n{corpo}\n"
        "    \\bottomrule\n  \\end{tabular}\n\\end{table}\n",
        encoding="utf-8",
    )


# ---------- busca de M ----------

def busca_m(pasta, saida):
    agg = agregar(ler(pasta, "busca_m.csv"), ["versao", "tamanho", "m"])
    for n, dados in agg.groupby("tamanho"):
        valores_m = sorted(dados.m.unique())
        posicao = {m: i for i, m in enumerate(valores_m)}  # M igualmente espaçados: a grade não é uniforme
        fig, (ax_tempo, ax_comp) = plt.subplots(1, 2, figsize=(7.5, 3))
        for versao in VERSOES:
            d = dados[dados.versao == versao]
            if d.empty:
                continue
            x = d.m.map(posicao)
            estilo_linha = dict(color=CORES[versao], marker=MARCADORES[versao], label=NOMES_VERSAO[versao])
            ax_tempo.errorbar(x, d.tempo_medio, yerr=d.tempo_desvio, capsize=2, elinewidth=1, **estilo_linha)
            ax_comp.plot(x, d.comparacoes, **estilo_linha)
            melhor = d.loc[d.tempo_medio.idxmin()]
            ax_tempo.annotate(f"M={int(melhor.m)}", (posicao[melhor.m], melhor.tempo_medio),
                              textcoords="offset points", xytext=(0, -14), ha="center", color=TINTA, fontsize=8)
        for ax in (ax_tempo, ax_comp):
            ax.set_xticks(range(len(valores_m)))
            ax.set_xticklabels([str(m) for m in valores_m])
            ax.set_xlabel("M")
        ax_tempo.set_ylabel("tempo médio (ms)")
        ax_tempo.set_title(f"Tempo (n = {inteiro(n)})")
        ax_comp.set_ylabel("comparações")
        ax_comp.set_title(f"Comparações (n = {inteiro(n)})")
        fig.legend(*ax_comp.get_legend_handles_labels(), loc="lower center", ncol=2, bbox_to_anchor=(0.5, -0.15))
        fig.savefig(saida / f"busca_m_n{n}.pdf")
        plt.close(fig)

        linhas = []
        for m in sorted(dados.m.unique()):
            linha = [str(m)]
            for versao in ("hibrido", "hibrido_mediana"):
                d = dados[(dados.versao == versao) & (dados.m == m)].iloc[0]
                linha += [decimal(d.tempo_medio, 4), decimal(d.tempo_desvio, 4), inteiro(d.comparacoes)]
            linhas.append(linha)
        tabela_tex(
            saida / f"tabela_busca_m_n{n}.tex",
            f"Busca de M em vetores aleatórios de {inteiro(n)} elementos (tempo em ms).",
            f"tab:busca-m-n{n}",
            ["M", "Híbrido: tempo", "desvio", "comparações", "Mediana-3: tempo", "desvio", "comparações"],
            linhas,
            "rrrrrrr",
        )


# ---------- comparação geral ----------

def barras_agrupadas(ax, dados, coluna, desvio=None):
    largura = 0.26
    x = np.arange(len(TIPOS))
    for i, versao in enumerate(VERSOES):
        d = dados[dados.versao == versao].set_index("tipo_vetor").reindex(TIPOS)
        ax.bar(x + (i - 1) * largura, d[coluna], largura - 0.03, color=CORES[versao], label=NOMES_VERSAO[versao],
               yerr=d[desvio] if desvio else None, error_kw=dict(elinewidth=0.8, capsize=1.5, ecolor=TINTA))
    ax.set_xticks(x)
    ax.set_xticklabels([NOMES_TIPO[t] for t in TIPOS], fontsize=8)
    ax.grid(axis="x", visible=False)


def comparacao(pasta, saida):
    agg = agregar(ler(pasta, "comparacao.csv"), ["versao", "tipo_vetor", "tamanho"])
    tamanhos = sorted(agg.tamanho.unique())

    fig, eixos = plt.subplots(1, len(tamanhos), figsize=(3.8 * len(tamanhos), 3.2))
    for ax, n in zip(np.atleast_1d(eixos), tamanhos):
        barras_agrupadas(ax, agg[agg.tamanho == n], "tempo_medio", "tempo_desvio")
        ax.set_yscale("log")
        ax.set_title(f"n = {inteiro(n)}")
    np.atleast_1d(eixos)[0].set_ylabel("tempo médio (ms, escala log)")
    fig.legend(*np.atleast_1d(eixos)[0].get_legend_handles_labels(), loc="lower center", ncol=3,
               bbox_to_anchor=(0.5, -0.06))
    fig.savefig(saida / "comparacao_tempo.pdf")
    plt.close(fig)

    maior = agg[agg.tamanho == tamanhos[-1]]
    fig, eixos = plt.subplots(1, 3, figsize=(11.4, 3.2))
    for ax, (coluna, titulo) in zip(eixos, [("comparacoes", "Comparações"), ("trocas", "Trocas"),
                                            ("deslocamentos", "Deslocamentos")]):
        barras_agrupadas(ax, maior, coluna)
        ax.set_yscale("log")
        ax.set_ylim(1, maior[coluna].max() * 5)  # contador zero fica sem barra; o valor exato está nas tabelas
        ax.set_title(f"{titulo} (n = {inteiro(tamanhos[-1])})")
    eixos[0].set_ylabel("quantidade (escala log)")
    fig.legend(*eixos[0].get_legend_handles_labels(), loc="lower center", ncol=3, bbox_to_anchor=(0.5, -0.06))
    fig.savefig(saida / "comparacao_contadores.pdf")
    plt.close(fig)

    for n in tamanhos:
        linhas = []
        for tipo in TIPOS:
            for versao in VERSOES:
                d = agg[(agg.tamanho == n) & (agg.tipo_vetor == tipo) & (agg.versao == versao)].iloc[0]
                linhas.append([NOMES_TIPO[tipo], NOMES_CURTOS[versao], decimal(d.tempo_medio), decimal(d.tempo_desvio),
                               inteiro(d.comparacoes), inteiro(d.trocas), inteiro(d.deslocamentos),
                               inteiro(d.trocas + d.deslocamentos)])
        tabela_tex(
            saida / f"tabela_comparacao_n{n}.tex",
            f"Comparação entre as versões com n = {inteiro(n)} (tempo em ms, média de 10 repetições). "
            "Movim. = trocas + deslocamentos.",
            f"tab:comparacao-n{n}",
            ["Tipo de vetor", "Versão", "Tempo", "Desvio", "Comparações", "Trocas", "Deslocamentos", "Movim."],
            linhas,
            "llrrrrrr",
        )


# ---------- crescimento ----------

def crescimento(pasta, saida):
    agg = agregar(ler(pasta, "crescimento.csv"), ["tamanho"]).sort_values("tamanho")
    n = agg.tamanho.to_numpy(dtype=float)
    cor = CORES["recursivo"]

    fig, (ax_comp, ax_tempo) = plt.subplots(1, 2, figsize=(7.5, 3))
    ax_comp.plot(n, agg.comparacoes, color=cor, marker="o", label="medido")
    ax_comp.plot(n, n * n / 2, color=TINTA, linestyle="--", linewidth=1, label="n²/2")
    ax_comp.set_ylabel("comparações")
    ax_comp.set_title("Comparações")
    ax_comp.legend(fontsize=8)
    ax_tempo.errorbar(n, agg.tempo_medio, yerr=agg.tempo_desvio, color=cor, marker="o", capsize=2, elinewidth=1)
    ax_tempo.set_ylabel("tempo médio (ms)")
    ax_tempo.set_title("Tempo")
    for ax in (ax_comp, ax_tempo):
        ax.set_xscale("log", base=2)
        ax.set_yscale("log")
        ax.set_xticks(n)
        ax.set_xticklabels([f"{int(x / 1000)}k" for x in n])
        ax.minorticks_off()
        ax.set_xlabel("n (vetor ordenado, escala log)")
    fig.savefig(saida / "crescimento.pdf")
    plt.close(fig)

    linhas = []
    anterior = None
    for _, d in agg.iterrows():
        razao_comp = decimal(d.comparacoes / anterior.comparacoes, 2) if anterior is not None else "--"
        razao_tempo = decimal(d.tempo_medio / anterior.tempo_medio, 2) if anterior is not None else "--"
        linhas.append([inteiro(d.tamanho), inteiro(d.comparacoes), razao_comp, decimal(d.tempo_medio),
                       decimal(d.tempo_desvio), razao_tempo])
        anterior = d
    tabela_tex(
        saida / "tabela_crescimento.tex",
        "Quicksort recursivo em vetores ordenados com n dobrando. As razões são em relação à linha anterior.",
        "tab:crescimento",
        ["n", "Comparações", "Razão", "Tempo (ms)", "Desvio", "Razão"],
        linhas,
        "rrrrrr",
    )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--resultados", type=Path, default=Path("resultados"))
    parser.add_argument("--saida", type=Path, default=Path("figuras"))
    args = parser.parse_args()
    args.saida.mkdir(parents=True, exist_ok=True)
    estilo()

    etapas = {"busca_m.csv": busca_m, "comparacao.csv": comparacao, "crescimento.csv": crescimento}
    for arquivo, etapa in etapas.items():
        if (args.resultados / arquivo).exists():
            etapa(args.resultados, args.saida)
            print(f"{arquivo}: gráficos e tabelas gerados em {args.saida}/")
        else:
            print(f"{arquivo}: não encontrado em {args.resultados}/, pulando")


if __name__ == "__main__":
    main()
