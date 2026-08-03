package com.megaac.plugin.listeners;

import com.megaac.plugin.managers.SpecManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class SpecItemListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!SpecManager.isSpectating(player.getUniqueId())) return;
        if (event.getItem() == null) return;

        Material type = event.getItem().getType();
        if (type == Material.COMPASS) {
            event.setCancelled(true);
            var targetId = SpecManager.getTarget(player.getUniqueId());
            Player target = targetId != null ? Bukkit.getPlayer(targetId) : null;
            if (target != null) {
                player.performCommand("cheking " + target.getName());
            }
        } else if (type == Material.BARRIER) {
            event.setCancelled(true);
            player.performCommand("specoff");
        }
    }
}
