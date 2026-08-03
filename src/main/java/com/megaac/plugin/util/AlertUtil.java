package com.megaac.plugin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AlertUtil {

    private static String alertPermission = "megaac.staff";
    private static String prefix = "§8[§cMegaAC§8] §r";

    public static void init(String permission, String msgPrefix) {
        alertPermission = permission;
        prefix = msgPrefix;
    }

    /**
     * Отправляет варнинг всем онлайн-игрокам, у которых есть право alertPermission
     * (LuckPerms права тоже проверяются через hasPermission - стандартный Bukkit API
     * подтягивает пермишены из любого permission-плагина, включая LuckPerms).
     */
    public static void broadcastAlert(Component message) {
        Component full = Component.text(prefix).append(message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission(alertPermission)) {
                p.sendMessage(full);
            }
        }
        Bukkit.getConsoleSender().sendMessage(full);
    }

    public static void broadcastAlert(String plain) {
        broadcastAlert(Component.text(plain).color(NamedTextColor.RED));
    }
}
