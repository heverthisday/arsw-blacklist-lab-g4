package edu.eci.arsw.blacklist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

class FixedPoolBlackListSearchTest {

    @Test
    void shouldMatchSequentialResultsWithPoolOfFour() {
        List<BlackListProvider> providers = ProviderFactory.create(100, false);
        BlackListSearch sequential = new SequentialBlackListSearch(providers);
        BlackListSearch fixedPool = new FixedPoolBlackListSearch(providers, 4);
        SearchResult sequentialResult = sequential.search("202.24.34.55", 5);
        SearchResult fixedPoolResult = fixedPool.search("202.24.34.55", 5);
        assertEquals(sequentialResult.matchingProviderIds(), fixedPoolResult.matchingProviderIds());

    }

    @Test
    void shouldMatchSequentialResultsWithPoolOfTwo() {
        List<BlackListProvider> providers = ProviderFactory.create(100, false);
        BlackListSearch sequential = new SequentialBlackListSearch(providers);
        BlackListSearch fixedPool = new FixedPoolBlackListSearch(providers, 2);
        SearchResult sequentialResult = sequential.search("202.24.34.55", 5);
        SearchResult fixedPoolResult = fixedPool.search("202.24.34.55", 5);
        assertEquals(sequentialResult.matchingProviderIds(), fixedPoolResult.matchingProviderIds());
    }

    @Test
    void shouldMatchSequentialResultsWithPoolOfEigth() {
        List<BlackListProvider> providers = ProviderFactory.create(100, false);
        BlackListSearch sequential = new SequentialBlackListSearch(providers);
        BlackListSearch fixedPool = new FixedPoolBlackListSearch(providers, 8);
        SearchResult sequentialResult = sequential.search("202.24.34.55", 5);
        SearchResult fixedPoolResult = fixedPool.search("202.24.34.55", 5);
        assertEquals(sequentialResult.matchingProviderIds(), fixedPoolResult.matchingProviderIds());
    }

    @Test
    void shouldReturnFasterResultsWithPoolOfSixteen() {
        List<BlackListProvider> providers = ProviderFactory.create(100, false);
        BlackListSearch sequential = new SequentialBlackListSearch(providers);
        BlackListSearch fixedPool = new FixedPoolBlackListSearch(providers, 16);
        SearchResult sequentialResult = sequential.search("202.24.34.55", 5);
        SearchResult fixedPoolResult = fixedPool.search("202.24.34.55", 5);
        assertEquals(sequentialResult.matchingProviderIds(), fixedPoolResult.matchingProviderIds());
    }

    @Test
    void shouldReturnMatchesInAscendingOrderWithoutDuplicates() {
        List<BlackListProvider> providers = ProviderFactory.create(100, false);
        BlackListSearch fixedPool = new FixedPoolBlackListSearch(providers, 8);
        SearchResult result = fixedPool.search("202.24.34.55", 5);

        List<Integer> sorted = new ArrayList<>(result.matchingProviderIds());
        Collections.sort(sorted);
        assertEquals(sorted, result.matchingProviderIds());

        Set<Integer> unique = new HashSet<>(result.matchingProviderIds());
        assertEquals(unique.size(), result.matchingProviderIds().size());
    }

    @Test
    void shouldRejectNonPositivePoolSize() {
        List<BlackListProvider> providers = ProviderFactory.create(100, false);
        assertThrows(IllegalArgumentException.class, () -> new FixedPoolBlackListSearch(providers, 0));
    }

    @Test
    void shouldConsultAllHundredProviders() {
        List<BlackListProvider> providers = ProviderFactory.create(100, false);
        BlackListSearch fixedPool = new FixedPoolBlackListSearch(providers, 4);
        SearchResult result = fixedPool.search("202.24.34.55", 5);
        assertEquals(100, result.consultedProviders());
    }
}