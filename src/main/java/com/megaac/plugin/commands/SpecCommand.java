package com.megaac.plugin.commands;

import com.megaac.plugin.managers.SpecManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SpecCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player moderator)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        if (args.length < 1) {
            moderator.sendMessage(Component.text("Использование: /spec <ник>").color(NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            moderator.sendMessage(Component.text("Игрок не найден или не в сети.").color(NamedTextColor.RED));
            return true;
        }

        SpecManager.startSpec(moderator, target);

        moderator.setGameMode(GameMode.SPECTATOR);
        moderator.teleport(target.getLocation());
        moderator.setSpectatorTarget(target);

        // Выдаём предметы наблюдения
        moderator.getInventory().clear();
        moderator.getInventory().setItem(0, namedItem(Material.COMPASS, "§eВызов на проверку", List.of("§7ПКМ - вызвать " + target.getName() + " на /cheking")));
        moderator.getInventory().setItem(8, namedItem(Material.BARRIER, "§cВыйти из наблюдения", List.of("§7ПКМ - завершить /spec")));

        moderator.sendMessage(Component.text("Вы наблюдаете за " + target.getName() + ". Инфо-панель справа.").color(NamedTextColor.GREEN));
        return true;
    }

    private ItemStack namedItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name.replace("§", "§").replace("&", "§")));
        List<Component> loreComponents = lore.stream().map(l -> Component.text(l.replace("&", "§"))).toList();
        meta.lore(loreComponents);
        item.setItemMeta(meta);
        return item;
    }
}
