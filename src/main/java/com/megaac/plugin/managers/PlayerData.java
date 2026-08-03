package com.megaac.plugin.managers;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

/**
 * Хранит служебную информацию и состояние проверок для одного игрока.
 */
public class PlayerData {

    public final UUID uuid;
    public String name;
    public InetSocketAddress ip;
    public String clientBrand = "unknown"; // НЕ лаунчер, а бренд клиента (vanilla/forge/fabric и т.д.)
    public long joinTime;

    // ---- Killaura / combat ----
    public final Deque<Long> recentHits = new ArrayDeque<>();       // таймстемпы ударов
    public final Deque<UUID> recentTargets = new ArrayDeque<>();    // цели последних ударов
    public float lastYaw;
    public float lastPitch;

    // ---- X-ray ----
    public int oreSuspicionScore = 0;
    public long oreWindowStart = System.currentTimeMillis();

    // ---- Модерация ----
    public int warnings = 0;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
}
