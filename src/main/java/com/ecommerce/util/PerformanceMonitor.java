package com.ecommerce.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PerformanceMonitor measures and records query execution times.
 * Used to generate performance comparison reports (before/after optimization).
 *
 * Design note: Stores timing data in a LinkedHashMap to preserve insertion order
 * for sequential reporting.
 */
public class PerformanceMonitor {
    private static final Map<String, List<Long>> timings = new LinkedHashMap<>();

    /**
     * Records a timing for a named query.
     * @param queryName descriptive name of the query being timed
     * @param durationMs execution time in milliseconds
     */
    public static void record(String queryName, long durationMs) {
        timings.computeIfAbsent(queryName, k -> new ArrayList<>()).add(durationMs);
    }

    /**
     * Calculates the average execution time for a named query.
     * @param queryName the query to get the average for
     * @return average time in ms, or 0 if no data available
     */
    public static double getAverage(String queryName) {
        List<Long> times = timings.get(queryName);
        if (times == null || times.isEmpty()) return 0;
        return times.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    /**
     * Returns the last recorded timing for a named query.
     */
    public static long getLastTiming(String queryName) {
        List<Long> times = timings.get(queryName);
        if (times == null || times.isEmpty()) return 0;
        return times.get(times.size() - 1);
    }

    /**
     * Returns all recorded timings.
     */
    public static Map<String, List<Long>> getAllTimings() {
        return new LinkedHashMap<>(timings);
    }

    /**
     * Clears all timing records.
     */
    public static void clear() {
        timings.clear();
    }

    /**
     * Generates a formatted performance summary string.
     */
    public static String generateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERFORMANCE REPORT ===\n");
        sb.append(String.format("Generated: %tF %<tT%n", System.currentTimeMillis()));
        sb.append("Methodology: Each query executed 5 times, average reported.\n\n");

        for (Map.Entry<String, List<Long>> entry : timings.entrySet()) {
            String name = entry.getKey();
            List<Long> times = entry.getValue();
            double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
            long min = times.stream().mapToLong(Long::longValue).min().orElse(0);
            long max = times.stream().mapToLong(Long::longValue).max().orElse(0);

            sb.append(String.format("%s:%n", name));
            sb.append(String.format("  Executions: %d%n", times.size()));
            sb.append(String.format("  Average: %.1fms%n", avg));
            sb.append(String.format("  Min: %dms | Max: %dms%n%n", min, max));
        }

        return sb.toString();
    }
}
