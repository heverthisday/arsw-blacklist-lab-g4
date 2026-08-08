package edu.eci.arsw.blacklist;

import java.util.ArrayList;
import java.util.List;

public final class BenchmarkRunner {
    private static final int PROVIDER_COUNT = 100;
    private static final int ALARM_THRESHOLD = 5;

    private BenchmarkRunner() {
    }

    public static void main(String[] args) {
        // --- 1. Leer y validar argumentos ---
        // Contrato: <strategy> <ipAddress> <simulateIo> <warmups> <measuredRuns> [poolSize]
        if (args.length < 5) {
            System.out.println("Usage: <SEQUENTIAL|FIXED|VIRTUAL> <ipAddress> <simulateIo> <warmups> <measuredRuns> [poolSize]");
            return;
        }

        String strategy = args[0].toUpperCase();
        String ipAddress = args[1];
        boolean simulateIo = Boolean.parseBoolean(args[2]);
        int warmups = Integer.parseInt(args[3]);
        int measuredRuns = Integer.parseInt(args[4]);

        int poolSize = 0;
        if (strategy.equals("FIXED")) {
            if (args.length < 6) {
                System.out.println("FIXED strategy requires a poolSize argument.");
                return;
            }
            poolSize = Integer.parseInt(args[5]);
        }

        // --- 2. Crear proveedores e instanciar la estrategia elegida ---
        List<BlackListProvider> providers = ProviderFactory.create(PROVIDER_COUNT, simulateIo);

        BlackListSearch search = switch (strategy) {
            case "SEQUENTIAL" -> new SequentialBlackListSearch(providers);
            case "FIXED" -> new FixedPoolBlackListSearch(providers, poolSize);
            case "VIRTUAL" -> new VirtualThreadBlackListSearch(providers);
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
        };

        // --- 3. Resultado de referencia, para verificar correctitud en cada corrida medida ---
        BlackListSearch reference = new SequentialBlackListSearch(providers);
        SearchResult expected = reference.search(ipAddress, ALARM_THRESHOLD);

        System.out.printf("Strategy: %s | poolSize: %s | IP: %s | simulateIo: %s | warmups: %d | measuredRuns: %d%n",
                strategy, strategy.equals("FIXED") ? poolSize : "-", ipAddress, simulateIo, warmups, measuredRuns);

        // --- 4. Warm-up runs: se ejecutan pero no se miden ni se reportan ---
        for (int i = 0; i < warmups; i++) {
            search.search(ipAddress, ALARM_THRESHOLD);
        }

        // --- 5. Measured runs: se miden, se validan contra el resultado esperado ---
        List<Double> elapsedMillis = new ArrayList<>();
        String scenario = simulateIo ? "IO" : "NoIO";

        for (int run = 1; run <= measuredRuns; run++) {
            SearchResult result = search.search(ipAddress, ALARM_THRESHOLD);

            if (!result.matchingProviderIds().equals(expected.matchingProviderIds())
                    || result.consultedProviders() != expected.consultedProviders()) {
                throw new IllegalStateException("Run " + run + " produced a result different from the sequential baseline");
            }

            double ms = result.elapsed().toNanos() / 1_000_000.0;
            elapsedMillis.add(ms);

            System.out.printf("Run %d: %.3f ms%n", run, ms);
            System.out.printf("%s,%s,%s,%d,%.3f,%d,%d%n",
                    scenario, strategy, strategy.equals("FIXED") ? poolSize : "-", run, ms,
                    result.matchingProviderIds().size(), result.consultedProviders());
        }

        // --- 6. Resumen: min, max, avg ---
        double min = elapsedMillis.get(0);
        double max = elapsedMillis.get(0);
        double sum = 0.0;
        for (double ms : elapsedMillis) {
            min = Math.min(min, ms);
            max = Math.max(max, ms);
            sum += ms;
        }
        double avg = sum / elapsedMillis.size();

        System.out.printf("Summary -> min: %.3f ms | max: %.3f ms | avg: %.3f ms%n", min, max, avg);
    }
}
