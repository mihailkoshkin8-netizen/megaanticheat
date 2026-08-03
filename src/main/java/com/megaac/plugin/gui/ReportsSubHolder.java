package com.megaac.plugin.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class ReportsSubHolder implements InventoryHolder {

    public final UUID targetId;
    public final String targetName;

    public ReportsSubHolder(UUID targetId, String targetName) {
        this.targetId = targetId;
        this.targetName = targetName;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
