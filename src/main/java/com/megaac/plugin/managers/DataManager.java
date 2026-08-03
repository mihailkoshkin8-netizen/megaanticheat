package com.megaac.plugin.managers;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {

    private static final Map<UUID, PlayerData> DATA = new ConcurrentHashMap<>();

    public static PlayerData get(UUID uuid, String fallbackName) {
        return DATA.computeIfAbsent(uuid, id -> new PlayerData(id, fallbackName));
    }

    public static PlayerData getOrNull(UUID uuid) {
        return DATA.get(uuid);
    }

    public static void remove(UUID uuid) {
        // Специально НЕ удаляем при выходе, чтобы копить статистику (например xray) между сессиями.
        // Если нужно чистить память на больших серверах - раскомментируйте:
        // DATA.remove(uuid);
    }
}
