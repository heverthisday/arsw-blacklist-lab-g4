package edu.eci.arsw.blacklist;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualThreadBlackListSearchTest {

    @Test
    void shouldFindSameMatchesAsSequential() {
        List<BlackListProvider> providers = buildProviders(100);
        String ipAddress = "202.24.34.55";
        int alarmThreshold = 5;

        SearchResult sequentialResult =
                new SequentialBlackListSearch(providers).search(ipAddress, alarmThreshold);
        SearchResult virtualThreadResult =
                new VirtualThreadBlackListSearch(providers).search(ipAddress, alarmThreshold);

        List<Integer> sequentialMatches = new ArrayList<>(sequentialResult.matchingProviderIds());
        List<Integer> virtualThreadMatches = new ArrayList<>(virtualThreadResult.matchingProviderIds());

        // Order can differ because tasks finish in different order, so we sort before comparing.
        sequentialMatches.sort(Integer::compareTo);
        virtualThreadMatches.sort(Integer::compareTo);

        assertEquals(sequentialMatches, virtualThreadMatches);
        assertEquals(sequentialResult.consultedProviders(), virtualThreadResult.consultedProviders());
    }

    @Test
    void shouldConsultEveryProvider() {
        List<BlackListProvider> providers = buildProviders(50);
        SearchResult result = new VirtualThreadBlackListSearch(providers)
                .search("10.0.0.1", 3);

        assertEquals(50, result.consultedProviders());
    }

    @Test
    void shouldRejectInvalidThreshold() {
        List<BlackListProvider> providers = buildProviders(10);
        VirtualThreadBlackListSearch search = new VirtualThreadBlackListSearch(providers);

        assertTrue(assertThrowsIllegalArgument(() -> search.search("10.0.0.1", 0)));
    }

    private static List<BlackListProvider> buildProviders(int count) {
        List<BlackListProvider> providers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            providers.add(new MockBlackListProvider(i, false, 0));
        }
        return providers;
    }

    private static boolean assertThrowsIllegalArgument(Runnable action) {
        try {
            action.run();
            return false;
        } catch (IllegalArgumentException ex) {
            return true;
        }
    }
}