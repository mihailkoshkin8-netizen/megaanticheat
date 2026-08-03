package com.megaac.plugin.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только для игроков.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Component.text("Использование: /tp <ник>").color(NamedTextColor.RED));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("Игрок не найден или не в сети.").color(NamedTextColor.RED));
            return true;
        }
        player.teleport(target.getLocation());
        player.sendMessage(Component.text("Телепортированы к " + target.getName()).color(NamedTextColor.GREEN));
        return true;
    }
}
