package com.megaac.plugin.listeners;

import com.megaac.plugin.managers.VanishManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class VanishJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        VanishManager.applyToJoiningPlayer(event.getPlayer());
    }
}
