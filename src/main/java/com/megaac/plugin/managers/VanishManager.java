package com.megaac.plugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ванишь: игрок становится невидим для всех, у кого нет права megaac.staff.
 * Повторный вызов /v выключает ванишь обратно.
 */
public class VanishManager {

    private static final Set<UUID> VANISHED = ConcurrentHashMap.newKeySet();
    private static Plugin plugin;
    private static String seePermission = "megaac.staff";

    public static void init(Plugin pluginInstance, String permission) {
        plugin = pluginInstance;
        seePermission = permission;
    }

    public static boolean isVanished(UUID uuid) {
        return VANISHED.contains(uuid);
    }

    /** @return true если ванишь включился, false если выключился */
    public static boolean toggle(Player player) {
        if (VANISHED.contains(player.getUniqueId())) {
            unvanish(player);
            return false;
        } else {
            vanish(player);
            return true;
        }
    }

    private static void vanish(Player player) {
        VANISHED.add(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(player) && !other.hasPermission(seePermission)) {
                other.hidePlayer(plugin, player);
            }
        }
    }

    private static void unvanish(Player player) {
        VANISHED.remove(player.getUniqueId());
        for (Player other : Bukkit.getOnlinePlayers()) {
            other.showPlayer(plugin, player);
        }
    }

    /** Прячет всех уже ваниш-игроков от только что зашедшего, если у него нет права видеть ванишь. */
    public static void applyToJoiningPlayer(Player joined) {
        if (joined.hasPermission(seePermission)) return;
        for (UUID vanishedId : VANISHED) {
            Player vanishedPlayer = Bukkit.getPlayer(vanishedId);
            if (vanishedPlayer != null && !vanishedPlayer.equals(joined)) {
                joined.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }
}
