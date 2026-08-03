package com.megaac.plugin.listeners;

import com.megaac.plugin.gui.ReportsMainHolder;
import com.megaac.plugin.gui.ReportsSubHolder;
import com.megaac.plugin.managers.GuiSessionManager;
import com.megaac.plugin.managers.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

/**
 * Подменю репортов конкретного игрока содержит 3 кнопки:
 *  - Удалить репорты (если накидали "по приколу")
 *  - Телепортироваться к игроку (/tp)
 *  - Наблюдать за игроком (/spec)
 * (два последних пункта добавлены как разумное дополнение к явно запрошенной кнопке удаления,
 *  чтобы из меню репортов можно было сразу отреагировать на жалобу).
 */
public class ReportsGuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;

        if (event.getView().getTopInventory().getHolder() instanceof ReportsMainHolder) {
            event.setCancelled(true);
            if (clickedInv != event.getView().getTopInventory()) return; // клик по своему инвентарю - игнор
            int slot = event.getSlot();
            UUID targetId = GuiSessionManager.getTargetForSlot(player.getUniqueId(), slot);
            if (targetId == null) return;
            openSubMenu(player, targetId, ReportManager.getTargetName(targetId));
            return;
        }

        if (event.getView().getTopInventory().getHolder() instanceof ReportsSubHolder holder) {
            event.setCancelled(true);
            if (clickedInv != event.getView().getTopInventory()) return;
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null) return;

            switch (clicked.getType()) {
                case BARRIER -> {
                    ReportManager.clearReports(holder.targetId);
                    player.sendMessage(Component.text("Репорты на " + holder.targetName + " удалены.").color(NamedTextColor.GREEN));
                    player.closeInventory();
                }
                case COMPASS -> {
                    player.closeInventory();
                    player.performCommand("tp " + holder.targetName);
                }
                case ENDER_EYE -> {
                    player.closeInventory();
                    player.performCommand("spec " + holder.targetName);
                }
                default -> {}
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getInventory().getHolder() instanceof ReportsMainHolder) {
            GuiSessionManager.clear(player.getUniqueId());
        }
    }

    private void openSubMenu(Player viewer, UUID targetId, String targetName) {
        Inventory inv = Bukkit.createInventory(new ReportsSubHolder(targetId, targetName), 9,
                Component.text("Действия: " + targetName));

        inv.setItem(2, namedItem(Material.BARRIER, "§cУдалить репорты",
                List.of("§7Если репорты накидали", "§7просто по приколу")));
        inv.setItem(4, namedItem(Material.COMPASS, "§eТелепортироваться к игроку",
                List.of("§7/tp " + targetName)));
        inv.setItem(6, namedItem(Material.ENDER_EYE, "§bНаблюдать за игроком",
                List.of("§7/spec " + targetName)));

        GuiSessionManager.setSubMenuTarget(viewer.getUniqueId(), targetId);
        viewer.openInventory(inv);
    }

    private ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name.replace("&", "§")));
        meta.lore(lore.stream().map(l -> Component.text(l.replace("&", "§"))).toList());
        item.setItemMeta(meta);
        return item;
    }
}
