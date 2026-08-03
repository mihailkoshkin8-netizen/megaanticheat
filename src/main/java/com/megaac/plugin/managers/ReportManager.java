package com.megaac.plugin.managers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReportManager {

    public record ReportEntry(String reporterName, String reason, long timestamp) {}

    private static final Map<UUID, String> TARGET_NAMES = new ConcurrentHashMap<>();
    private static final Map<UUID, List<ReportEntry>> REPORTS = new ConcurrentHashMap<>();

    public static void addReport(UUID targetId, String targetName, String reporterName, String reason) {
        TARGET_NAMES.put(targetId, targetName);
        REPORTS.computeIfAbsent(targetId, id -> new CopyOnWriteArrayList<>())
                .add(new ReportEntry(reporterName, reason, Instant.now().toEpochMilli()));
    }

    public static List<ReportEntry> getReports(UUID targetId) {
        return REPORTS.getOrDefault(targetId, List.of());
    }

    public static void clearReports(UUID targetId) {
        REPORTS.remove(targetId);
    }

    public static String getTargetName(UUID targetId) {
        return TARGET_NAMES.getOrDefault(targetId, "Неизвестно");
    }

    /** Все игроки, на которых есть хотя бы один репорт (без пустых записей). */
    public static Map<UUID, String> getTargetsWithReports() {
        Map<UUID, String> result = new ConcurrentHashMap<>();
        for (Map.Entry<UUID, List<ReportEntry>> entry : REPORTS.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                result.put(entry.getKey(), TARGET_NAMES.getOrDefault(entry.getKey(), "?"));
            }
        }
        return result;
    }
}
