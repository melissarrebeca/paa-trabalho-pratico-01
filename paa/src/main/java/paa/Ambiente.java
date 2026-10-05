package paa;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Descrição da máquina gravada no topo de cada CSV, para a metodologia do relatório. */
final class Ambiente {

    private Ambiente() {
    }

    static List<String> descrever(long pilhaBytes) {
        List<String> linhas = new ArrayList<>();
        linhas.add("data: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        linhas.add("cpu: " + campoDoProc("/proc/cpuinfo", "model name"));
        linhas.add("nucleos: " + Runtime.getRuntime().availableProcessors());
        linhas.add("ram: " + campoDoProc("/proc/meminfo", "MemTotal"));
        linhas.add("so: " + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " " + System.getProperty("os.arch"));
        linhas.add("jvm: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.version"));
        linhas.add("flags: " + String.join(" ", ManagementFactory.getRuntimeMXBean().getInputArguments()));
        linhas.add("heap maximo: " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + " MB");
        linhas.add("pilha da thread de experimentos: " + pilhaBytes / (1024 * 1024) + " MB");
        return linhas;
    }

    /** Lê "campo : valor" de um arquivo do /proc (Linux); fora do Linux devolve "desconhecido". */
    private static String campoDoProc(String arquivo, String campo) {
        try (Stream<String> linhas = Files.lines(Path.of(arquivo))) {
            return linhas.filter(l -> l.startsWith(campo))
                    .map(l -> l.substring(l.indexOf(':') + 1).trim())
                    .findFirst()
                    .orElse("desconhecido");
        } catch (IOException | RuntimeException e) {
            return "desconhecido";
        }
    }
}
