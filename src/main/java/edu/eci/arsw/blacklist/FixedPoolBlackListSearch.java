package edu.eci.arsw.blacklist;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class FixedPoolBlackListSearch implements BlackListSearch {
    private final List<BlackListProvider> providers;
    private final int poolSize;

    public FixedPoolBlackListSearch(List<BlackListProvider> providers, int poolSize) {
        this.providers = List.copyOf(Objects.requireNonNull(providers, "providers"));
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be greater than zero");
        }
        this.poolSize = poolSize;
    }

    @Override
    public SearchResult search(String ipAddress, int alarmThreshold) {
        Objects.requireNonNull(ipAddress, "ipAddress");
        if (alarmThreshold <= 0) {
            throw new IllegalArgumentException("alarmThreshold must be greater than zero");
        }

        long startedAt = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (BlackListProvider provider : providers) {
                futures.add(executor.submit(() -> provider.isBlacklisted(ipAddress)));
            }

            List<Integer> matches = new ArrayList<>();
            int consulted = 0;
            for (int i = 0; i < futures.size(); i++) {
                consulted++;
                try {
                    if (futures.get(i).get()) {
                        matches.add(providers.get(i).id());
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Search was interrupted", ex);
                } catch (java.util.concurrent.ExecutionException ex) {
                    throw new IllegalStateException("Provider consultation failed", ex.getCause());
                }
            }

            Duration elapsed = Duration.ofNanos(System.nanoTime() - startedAt);
            return new SearchResult(ipAddress, matches, consulted, elapsed);
        } finally {
            executor.shutdown();
        }
    }
}