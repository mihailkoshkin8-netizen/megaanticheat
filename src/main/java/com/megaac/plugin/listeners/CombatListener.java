package com.megaac.plugin.listeners;

import com.megaac.plugin.managers.CheckingManager;
import com.megaac.plugin.managers.DataManager;
import com.megaac.plugin.managers.PlayerData;
import com.megaac.plugin.util.AlertUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Эвристические проверки на killaura и reach.
 * Это упрощённая, легко настраиваемая база - не полноценная замена
 * профессиональным анти-читам вроде Vulcan/Grim, но рабочий каркас,
 * который можно дорабатывать (пороговые значения в config.yml).
 */
public class CombatListener implements Listener {

    private final boolean killauraEnabled;
    private final int maxCps;
    private final double maxAngleDiff;
    private final long multiAuraWindowMs;

    private final boolean reachEnabled;
    private final double maxReach;

    public CombatListener(boolean killauraEnabled, int maxCps, double maxAngleDiff, long multiAuraWindowMs,
                           boolean reachEnabled, double maxReach) {
        this.killauraEnabled = killauraEnabled;
        this.maxCps = maxCps;
        this.maxAngleDiff = maxAngleDiff;
        this.multiAuraWindowMs = multiAuraWindowMs;
        this.reachEnabled = reachEnabled;
        this.maxReach = maxReach;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        // Игрока на проверке (/cheking) нельзя убить или нанести ему урон - он "заморожен" для боя
        if (event.getEntity() instanceof Player victim && CheckingManager.isBeingChecked(victim.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity victim)) return;

        // Атакующий, находящийся "на проверке", тоже не должен наносить урон
        if (CheckingManager.isBeingChecked(attacker.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        PlayerData data = DataManager.get(attacker.getUniqueId(), attacker.getName());
        long now = System.currentTimeMillis();

        if (reachEnabled) {
            double distance = attacker.getEyeLocation().distance(victim.getLocation().add(0, victim.getHeight() / 2.0, 0));
            if (distance > maxReach) {
                AlertUtil.broadcastAlert(Component.text(attacker.getName() + " — подозрение Reach ")
                        .color(NamedTextColor.RED)
                        .append(Component.text(String.format("(%.2f блоков)", distance)).color(NamedTextColor.GRAY)));
            }
        }

        if (killauraEnabled) {
            data.recentHits.addLast(now);
            data.recentTargets.addLast(victim.getUniqueId());
            while (!data.recentHits.isEmpty() && now - data.recentHits.peekFirst() > 1000) {
                data.recentHits.pollFirst();
                data.recentTargets.pollFirst();
            }

            // 1) Слишком высокий CPS
            if (data.recentHits.size() > maxCps) {
                AlertUtil.broadcastAlert(Component.text(attacker.getName() + " — подозрение Killaura ")
                        .color(NamedTextColor.RED)
                        .append(Component.text("(CPS: " + data.recentHits.size() + ")").color(NamedTextColor.GRAY)));
            }

            // 2) Угол взгляда не совпадает с направлением на цель в момент удара
            Vector toTarget = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
            Vector look = attacker.getEyeLocation().getDirection().normalize();
            double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, toTarget.dot(look)))));
            if (angle > maxAngleDiff) {
                AlertUtil.broadcastAlert(Component.text(attacker.getName() + " — подозрение Killaura ")
                        .color(NamedTextColor.RED)
                        .append(Component.text(String.format("(удар без взгляда на цель, угол %.1f°)", angle)).color(NamedTextColor.GRAY)));
            }

            // 3) Мульти-аура: удары по разным целям в узком временном окне
            long distinctRecentTargets = data.recentTargets.stream().distinct().count();
            if (distinctRecentTargets >= 2) {
                long windowHits = data.recentHits.stream().filter(t -> now - t <= multiAuraWindowMs).count();
                if (windowHits >= 2) {
                    AlertUtil.broadcastAlert(Component.text(attacker.getName() + " — подозрение Multi-Aura ")
                            .color(NamedTextColor.RED)
                            .append(Component.text("(несколько целей почти одновременно)").color(NamedTextColor.GRAY)));
                }
            }
        }
    }
}
