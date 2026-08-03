package com.megaac.plugin.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ReportsMainHolder implements InventoryHolder {
    @Override
    public Inventory getInventory() {
        return null; // не используется, инвентарь создаётся отдельно и хранит ссылку сам
    }
}
