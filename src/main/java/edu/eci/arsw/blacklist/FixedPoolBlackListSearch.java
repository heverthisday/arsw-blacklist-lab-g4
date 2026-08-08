package edu.eci.arsw.blacklist;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Laboratory implementation: students must complete this class.
 */
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
        long starAt = System.nanoTime();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        List<Future<Boolean>> futures = new ArrayList<>();
        for (BlackListProvider provider : providers) {
            Future<Boolean> future = executor.submit(() -> provider.isBlacklisted(ipAddress));
            futures.add(future);
        }

        List<Integer> matches = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                boolean result = futures.get(i).get();
                if (result) {
                    matches.add(providers.get(i).id());
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("BlackListSearch was interrupted", ex);
            } catch (ExecutionException ex) {
                throw new IllegalStateException("BlackListSearch failed", ex);
            }
        }

        Collections.sort(matches);
        executor.shutdown();
        Duration elapsed = Duration.ofNanos(System.nanoTime() - starAt);
        return new SearchResult(ipAddress, matches, providers.size(), elapsed);
    }
}
