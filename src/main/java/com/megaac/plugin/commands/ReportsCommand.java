package com.megaac.plugin.commands;

import com.megaac.plugin.gui.ReportsMainHolder;
import com.megaac.plugin.managers.GuiSessionManager;
import com.megaac.plugin.managers.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReportsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }

        Map<UUID, String> targets = ReportManager.getTargetsWithReports();
        if (targets.isEmpty()) {
            viewer.sendMessage(Component.text("Активных репортов нет.").color(NamedTextColor.GREEN));
            return true;
        }

        int size = Math.min(54, Math.max(9, ((targets.size() - 1) / 9 + 1) * 9));
        Inventory inv = Bukkit.createInventory(new ReportsMainHolder(), size,
                Component.text("Репорты игроков"));

        Map<Integer, UUID> slotMap = new HashMap<>();
        int slot = 0;
        SimpleDateFormat fmt = new SimpleDateFormat("dd.MM HH:mm");

        List<Map.Entry<UUID, String>> sorted = new ArrayList<>(targets.entrySet());
        sorted.sort(Comparator.comparing(Map.Entry::getValue));

        for (Map.Entry<UUID, String> entry : sorted) {
            if (slot >= size) break; // больше 54 репортов одновременно в меню не влезет
            UUID targetId = entry.getKey();
            String targetName = entry.getValue();
            var reports = ReportManager.getReports(targetId);

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("Репортов: " + reports.size()).color(NamedTextColor.GRAY));
            lore.add(Component.empty());
            int shown = 0;
            for (var r : reports) {
                if (shown >= 8) {
                    lore.add(Component.text("... и ещё " + (reports.size() - shown)).color(NamedTextColor.DARK_GRAY));
                    break;
                }
                lore.add(Component.text(fmt.format(new Date(r.timestamp())) + " " + r.reporterName() + ": " + r.reason())
                        .color(NamedTextColor.WHITE));
                shown++;
            }
            lore.add(Component.empty());
            lore.add(Component.text("Нажмите, чтобы открыть меню действий").color(NamedTextColor.YELLOW));

            ItemStack book = new ItemStack(Material.BOOK);
            ItemMeta meta = book.getItemMeta();
            meta.displayName(Component.text(targetName).color(NamedTextColor.GOLD));
            meta.lore(lore);
            book.setItemMeta(meta);

            inv.setItem(slot, book);
            slotMap.put(slot, targetId);
            slot++;
        }

        GuiSessionManager.setMainMenuSlots(viewer.getUniqueId(), slotMap);
        viewer.openInventory(inv);
        return true;
    }
}
